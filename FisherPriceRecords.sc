FisherPriceRecords {

	classvar <allNotes, <allowedNotes, playableDegrees;

	var noteEvents, transposition, lowestNote, degrees, str, <>title,
	source, lookupTable, <dur, realisedLength, <success;

	*initClass {

		//C 	C♯ 	D 	E♭ 	E 	F 	F♯ 	G 	G♯ 	A 	B♭ 	B 	C
		//C 	C# 	D 	D# 	E 	F 	F# 	G 	G# 	A 	A# 	B
		var index, doubles, notenames = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];

		allNotes = 127.collect({|i|
			(notenames.wrapAt(i) ++ (i.div(12) + 2)).asSymbol;
		});

		allowedNotes = ["D#4", "G#4", "A#4", "C5", "D#5", "F5", "G5", "G#5", "A#5", "C6", "C#6", "D#6", "F6", "G6", "G#6", "A#6"].collect({|note|
			allNotes.indexOf(note.asSymbol);
		});

		playableDegrees = allowedNotes - allowedNotes[0];

		doubles = ["G#5", "A#5", "C6", "C#6", "D#6", "F6"];

		doubles.do({|note|
			index = allNotes.indexOf(note.asSymbol);
			allNotes[index] =
			Pseq([note.asSymbol, (note ++ "_2").asSymbol], inf).asStream;
		});

	}

	* new{|events, title, source, length|

		^super.new.init(events, title, source, length);
	}

	* openMIDI{|file, title|
		^super.new.initFile(file, title);
	}

	* fromPattern{|pattern, title, length|
		^super.new.initPattern(pattern, title, length);
	}

	initPattern{|pattern, name, length|

		var events, event, defaultEvent, time, stream, eLIMIT, tLIMIT,
		warned, note;

		// This method borrows heavily from wslib

		eLIMIT = 360;
		tLIMIT = 60;
		warned = false;

		time = 0;
		events=[];
		stream = pattern.asStream;
		/*
		(length.isNil || (length==0)).if({
		"Infinite patterns will be truncated after % events".format(eLIMIT).warn;
		warned = true;
		}, { */
		(length > tLIMIT).if({
			"Your specified duration % is significantly longer than a single record revolution but will be scaled to fit in one.\nIt will be truncated after % events".warn(length, eLIMIT);
			warned = true;
		}, {
			(length.notNil && (length>0)).if({ tLIMIT = length; });
		});
		//});
		warned.if({
			"""Events that are too close together will fail to render correctly in
openScad or may not play back reliably.""".postln;
		});

		defaultEvent = Event.default;
		//defaultEvent.postln;

		defaultEvent.use({
			defaultEvent[ \midinote2 ] = {
				//"freq is %".format(defaultEvent[\freq]).postln;
				//"midinote is %".format(defaultEvent[\midinote]).postln;
				( ~freq.isFunction.not).if(

					{ ~freq.cpsmidi },
					{ ~midinote.value }
				);
			};
		});


		{(event = stream.next( defaultEvent )).notNil &&
			(events.size < eLIMIT) && (time < tLIMIT)}.while({
			//"loop".postln;
			event.use({
				event.isRest.not.if({

					[time, event[\midinote2].value]
					.multiChannelExpand.do({|arr|
						//"adding".postln;
						events = events.add(arr);
					});

					//event.postln;
				});
				//event.delta.postln;
				//time.postln;
				time = time+event.delta;
			});
		});

		(length.isNil || (length==0)).if({
			dur = time;
		}, {
			(time > tLIMIT).if({
				"% seconds truncated from the last event in your pattern to fit within the time limit.".format(time-tLIMIT).warn;
			});
			dur = length;
		});

		this.init(events, name);

		/*
		while { (event = stream.next( defaultEvent )).notNil &&
		{ (count = count use+ 1) <= maxEvents } }
		{ event.use({
		if( event.isRest.not ) // not a \rest
		{ 	[
		event.midinote2,
		event.velocity,
		time,
		event.sustain,
		event.upVelo, // addNote copies noteNumber if nil
		event.channel ? 0,
		event.track ?? {format.min(1)}, // format 0: all in track 0
		false // don't sort (yet)
		].multiChannelExpand // allow multi-note events
		.do({ |array| this.addNote( *array ); });

		};
		time = time + event.delta;

		});
		};
		*/

	}

	initFile{|file, name|

		var midi, events, start, type, note, velocity;

		Class.findAllReferences(\SimpleMIDIFile).notNil.if({
			midi = SimpleMIDIFile.read(file);
			midi.timeMode = \seconds;
			events = midi.midiEvents.select({|evt|
				start = evt[1];
				type = evt[2];
				note = evt[4];
				velocity = evt[5];

				(type == \noteOn) && ( velocity != 0)
			});

			events = events.collect({|evt|
				start = evt[1];
				type = evt[2];
				note = evt[4];
				velocity = evt[5];

				[start, note]
			});

			dur = midi.length;

		}, {

			"You must install the wslib Quark to load a MIDI file".warn;
		});

		this.init(events, name, file.toString);

	}

	init {|noteArr, name, src, length|

		noteEvents = noteArr;
		title = name;
		source = src;
		length.notNil.if({
			dur = length;
		});
		//str="";
		this.pr_de_dup();
		this.findTransposition;

	}


	pr_de_dup{

		var notes, events, note, start, velocity, chord, unique, min, max;

		notes = Dictionary();

		max = 0;
		min = inf;

		events = noteEvents.select({|evt|
			start = evt[0];
			note = evt[1];

			unique = true;
			chord = notes[start.asFloat];
			chord.notNil.if({
				unique = chord.includes(note).not;
			}, {
				chord = [];
			});
			chord = chord ++ note;
			notes[start.asFloat] = chord;

			min = min.min(note);
			max = max.max(note);

			unique


		}); // is a unique note with velocity

		noteEvents = events.sort({|a, b|
			a[0] < b[0]
		});

		//"max: % \tmin: %".format(max, min).postln;

	}

	length_{|length|
		dur = length;
		str = nil;
	}

	length{
		^dur
	}

	realize{|repeats=1, length|
		this.realise(repeats, length);
	}

	realise{|repeats=1, length|

		var note, time, noteStr;

		success.not.if({
			"Some notes are not playable and will not be printed".warn;
		});

		length.isNil.if({
			length = dur;
			//length.isNil.if({repeats = 1; length = 0;});
		});

		length.notNil.if({
			(length < noteEvents.last[0]).if({
				"length set shorter than the duration of the piece".warn;
				length = noteEvents.last[0] + 0.5;
			}, {
				realisedLength = repeats * length;
			})
		}, { length = noteEvents.last[0] + 0.5; });

		repeats.do({|i|
			noteEvents.do({|n|
				note = allNotes[this.map(n[1])].next;
				time = n[0] + (i * length);

				noteStr = "[[\"%\"], %],\n".format(note, time);
				str = str ++ noteStr;
			});
		});
	}

	map{ |note|

		var mapped;

		//note.postln;

		mapped = note + transposition;

		allowedNotes.indexOf(mapped).isNil.if({
			"note % is not in the key!!".format(allNotes[note].next).warn;
			note = \rest; //Rest();
		});

		^ note + transposition
	}

	p {|def, length = 0|

		var events, notes, startTime, item, onsets;

		def.isNil.if({ def = \fisherprice });
		def = def.asSymbol;


		notes = Dictionary.new;
		onsets = [];

		noteEvents.do({|evt|
			startTime = evt[0].asFloat;
			item = notes.at(startTime);
			item.isNil.if({
				item = [];
			});
			notes[startTime] = item ++ this.map(evt[1]);

			onsets.indexOf(startTime).isNil.if({
				onsets = onsets ++ startTime;
			});
		});

		onsets.sort;
		//onsets = onsets ++ length;
		(length == 0).if({
			dur.notNil.if({
				length = dur;
			}, {
				length = onsets.last + 0.5;
			});
		});


		^Pdef(def,
			Pbind(
				[\dur, \midinote], Prout({
					var last, note, dur, next, size;

					inf.do({

						last = onsets.first;

						(last > 0).if({
							[last, Rest(last)].yield;
						});

						size = onsets.size -1;

						onsets.do({|onset, index|
							note = notes[onset.asFloat];
							//note.postln;
							(index < size).if({
								dur = onsets[index + 1] - onset;
							}, {
								dur = length - onset;
							});

							[dur, note].yield;
						});
					})
				})
			)
		)
	}




	crushRange {|octaves|
		// -1 = raise lowest octave
		// -2 raise lowest two octaves
		// 1 lower highest octave
		// 2 lower highest two octaves
		// f.crushRange(2) gives the same result as
		//f.crushRange(1).crushRange(1)

		// to do

	}



	findTransposition {
		var notes, note, range, tryTransposition, errors, trans, found, min, best, outside;

		notes = [];
		transposition = 0;
		found = false;

		noteEvents.do({|evt|
			//(evt[2] == \noteOn).if ({
			note = evt[1];
			notes.includes(note).not.if({
				notes = notes ++ note;
			})
			//})
		});
		notes = notes.sort;

		range = notes.last - notes.first;

		//range.postln;
		//(allowedNotes.first - allowedNotes.last).abs.postln;

		(range.abs > (allowedNotes.first - allowedNotes.last).abs).if({
			"range too wide".warn;
		}, {

			tryTransposition = {|tnsp|

				var index, err, err_count;

				err = [];
				err_count = 0;

				//notes.postln;

				notes.do({|note|

					index = allowedNotes.indexOf(note + tnsp);
					index.isNil.if({
						err = err ++ note;
						err_count = err_count+1;

					});
				});

				(err.size == 0).if({
					//"success %".format(tnsp).postln;
				}, {
					//"transposition % failed".format(tnsp).postln;
				});

				[tnsp, err_count, err];
			};

			// Do we actually need to transpose?
			errors = tryTransposition.value(0);
			(errors[1] == 0).if({
				found = true;
				transposition = 0;
			}, {
				//search for a working transposition

				errors = [];

				allowedNotes.reverse.do({|allowed|

					trans = allowed - notes.last;
					errors = errors.add(tryTransposition.value(trans));
					//trans.postln;
					//errors.postln;
				});

				found = false;
				min = inf;

				errors.do({|item, index|


					found.not.if({
						(item[1] == 0).if({
							found = true;
							transposition = item.first;
							// count allowedNotes from end
							//allowedNotes.reverse[index] - notes.last;
							//"success %".format(transposition).postln;
						}, {
							(item[1] < min).if({
								min = item[1];
								best = [index];
								//"min is %".format(min).postln;
							}, { (item[1] == min).if({
								best = best ++ index;
							})
							})
						})
					});
				});
			});

			found.not.if({
				"no transposition found".warn;

				(min < inf).if({
					outside = [];
					best.do({|b|
						outside = outside.add(
							errors[b].last.collect({|n|
								allNotes[n].next;
						}));
					});
					"best has outside notes: %".format(outside).postln;
				})


			});
		});

		success = found;
		^transposition

	}

	write{|filename|

		var path, file, include, size;

		str.isNil.if({
			this.realise;
		});

		filename = filename.standardizePath;

		path = PathName(filename);

		file = File(filename, "w");
		file.write("// OpenSCAD file - http://www.openscad.org\n");

		source.notNil.if({
			file.write("// converted from % by SuperCollider".format(source.asString));
		}, {
			file.write("// converted / generated by SuperCollider\n");
		});

		file.write("""//
include <fpRecordModule.scad>

// Notes you can use:
// notes with a \"_2\" are double and can also be written without the \"_2\".
//
//C5, F5, G5,
//C6_2, F6_2, G6,
//
//D#4, G#4, A#4,
//D#5, G#5_2, A#5_2,
//C#6_2, D#6_2, G#6,A#6,
//the b is represented as a -
//E-4, A-4, B-4,
//E-5, A-5_2, B-5_2,
//D-6_2, E-6_2, A-6, B-6

composition = [
""" );
		file.write(this.asString);
		file.write("];\n\n\n");

		//realisedLength
		realisedLength.notNil.if({(realisedLength > 0).if({
			file.write("totalLength = %;\n\n".format(realisedLength));
		})});
		size = 4;
		(title.size <= 10).if({
			size = 5;
		});
		file.write("writeText(\"%\", y=8, size=%);\n".format(title, size));

		file.write("""

// That's all!
""");

		file.close;

		include = PathName(this.class.filenameSymbol.asString).pathOnly;
		include = include.asString ++ "fpRecordModule.scad";
		//include.postln;
		//path.pathOnly.asString.postln;
		{
			File.copy(include, path.pathOnly.asString);
		}.try({
			"Please ensure that there is a copy of fpRecordModule.scad in %.".format(path.pathOnly.asString).postln;
		});
	}

	asString{

		var ret;

		ret = str;

		ret.isNil.if({
			success.if({
				this.realise;
				ret = str;
			}, {
				ret = "a %".format(this.class.asString);
			});
		});

		^ret
	}
}
