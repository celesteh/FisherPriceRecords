FisherPriceRecords {

	classvar <allNotes, <allowedNotes, playableDegrees;

	var midiEvents, transposition, lowestNote, degrees, str, <>title,
	source, lookupTable;

	*initClass {

		//C 	C♯ 	D 	E♭ 	E 	F 	F♯ 	G 	G♯ 	A 	B♭ 	B 	C
		//C 	C# 	D 	D# 	E 	F 	F# 	G 	G# 	A 	A# 	B
		var index, doubles, notenames = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];

		allNotes = 127.collect({|i|
			(notenames.wrapAt(i) ++ (i.div(12) + 2)).asSymbol;
		});

		allowedNotes = ["D#4", "G#4", "A#4", "C5", "D#5", "F5", "G#5", "A#5", "C6", "C#6", "D#6", "F6", "G6", "G#6", "A#6"].collect({|note|
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

	* new{|midiEvents, title, source|

		^super.new.init(midiEvents, title, source);
	}

	init {|midiArr, name, src|

		midiEvents = midiArr;
		title = name;
		source = src;
		str="";
		this.pr_de_dup();
		this.findTransposition;

	}

	pr_de_dup{

		var notes, events, note, start, velocity, chord, unique;

		notes = Dictionary();

		events = midiEvents.select({|evt|
			start = evt[1];
			note = evt[4];
			velocity = evt[5];

			unique = true;
			chord = notes[start.asFloat];
			chord.notNil.if({
				unique = chord.includes(note).not;
			}, {
				chord = [];
			});
			chord = chord ++ note;
			notes[start.asFloat] = chord;


			(evt[2] == \noteOn) && (velocity != 0) && unique


		}); // is a unique note with velocity

		midiEvents = events.sort({|a, b|
			a[1] < b[1]
		});


	}

	realise{|repeats=1, length|

		var noteEvents, note, time, noteStr;

		length.isNil.if({ repeats = 1; length = 0;});


		repeats.do({|i|
			midiEvents.do({|n|
					note = allNotes[this.map(n[4])].next;
					time = n[1] + (i * length);

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
			note = Rest();
		});

		^ note + transposition
	}

	p {|def, length = 0|

		var events, notes, startTime, item, onsets;

		def.isNil.if({ def = \fisherprice });
		def = def.asSymbol;


		notes = Dictionary.new;
		onsets = [];

		midiEvents.do({|evt|
			startTime = evt[1].asFloat;
			item = notes.at(startTime);
			item.isNil.if({
				item = [];
			});
			notes[startTime] = item ++ this.map(evt[4]);

			onsets.indexOf(startTime).isNil.if({
				onsets = onsets ++ startTime;
			});
		});

		onsets.sort;
		//onsets = onsets ++ length;
		(length == 0).if({
			length = onsets.last + 0.5;
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
		var notes, note, range, tryTransposition, errors, trans, found, min, best;

		notes = [];
		errors = [];

		midiEvents.do({|evt|
			(evt[2] == \noteOn).if ({
				note = evt[4];
				notes.includes(note).not.if({
					notes = notes ++ note;
				})
			})
		});
		notes = notes.sort;

		range = notes.last - notes.first;

		(range.abs > (allowedNotes.first - allowedNotes.last).abs).if({
			"range too wide".warn;
		}, {

			tryTransposition = {|tnsp|

				var index, err;

				err = [];

				//notes.postln;

				notes.do({|note|

					index = allowedNotes.indexOf(note + tnsp);
					index.isNil.if({
						err = err ++ note;

					});
				});

				(err.size == 0).if({
					"success %".format(tnsp).postln;
				}, {
					//"transposition % failed".format(tnsp).postln;
				});

				err;
			};

			allowedNotes.reverse.do({|allowed|

				trans = allowed - notes.last;
				errors = errors ++ tryTransposition.value(trans);
			});

			found = false;

			errors.do({|item, index|

				found.not.if({
					(item.size == 0).if({
						found = true;
						transposition = // count allowedNotes from end
						allowedNotes.reverse[index] - notes.last;
					})
				})
			});



		});

		found.not.if({
			"no transposition found".warn;
			(errors.size > 0).if({
				min = inf;
				best = 0;
				errors.do({|item, index|
					(item.size < min).if({
						min = item.size;
						best = index;
					});
				});
				"best has outside notes: %".format(errors[best]);
			});

			transposition = 0;
		});

		^transposition

	}

	write{|filename|

		var path, file, include;

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
		file.write("writeText(\"%\", y=8, size=4);\n".format(title));

		file.write("""

// That's all!
""");

		file.close;

		include = PathName(FisherPriceRecords.filenameSymbol.asString).pathOnly;
		include = include.asString ++ "fpRecordModule.scad";
		include.postln;
		File.copy(include, path.pathOnly);
	}

	asString{

		str.isNil.if({
			this.realise
		});

		^str
	}
}