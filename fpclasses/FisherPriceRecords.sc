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
		this.findTransposition.postln;
		//this.realise;

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

		//noteEvents = midiEvents.select({|evt|
		//	(evt[2] == \noteOn)
		//});

		repeats.do({|i|
			//noteEvents.do({|n|
			//	(n[5]!= 0).if({ // velocity is not 0
			midiEvents.do({|n|
					note = allNotes[this.map(n[4])].next;
					time = n[1] + (i * length);

					noteStr = "[[\"%\"], %],\n".format(note, time);
					str = str ++ noteStr;
				});
			//})
		});
	}

	/*
	map {|note|

	var degree, output, i;

	degree = note - lowestNote + transposition;

	degree.postln;

	i = 1;

	output = allowedNotes[degree];
	{ output.isNil && (i < 5)}.while ({
	output = allowedNotes[degree + (12 * i)];
	output.isNil.if({
	output = allowedNotes[degree -(12 * i)];
	output.isNil.if({
	i = i + 1;
	})})});

	output.isNil.if({ "this shouldn't happen".warn; });

	^output

	}
	*/

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

	/*
	p {|def|
	var onsets, dict, evt, notes, events;

	events = midiEvents.select({|evt|
	(evt[2] == \noteOn) && (evt[5] != 0 )
	}).sort({|a, b| a[1] < b[1] });

	dict = Dictionary.new;

	events.do({|e|
	evt = dict.at(e[1]);
	evt.isNil.if({ evt = [] });
	//e[4].postln;
	evt = evt ++ this.map(e[4]);
	dict.put(e[1], evt);
	});

	onsets = dict.keys.asArray.sort;
	notes = onsets.collect({|o| dict.at(o); });
	onsets = onsets.differentiate;

	def.isNil.if({ def = \fisherprice });
	def = def.asSymbol;

	^Pdef(def,
	Pbind(
	\dur, Pseq(onsets, inf),
	\midinote, Pseq(notes, inf) + 12
	)
	);
	}
	*/
	p {|def, length = 0|

		var events, notes, startTime, item, onsets;

		def.isNil.if({ def = \fisherprice });
		def = def.asSymbol;

		//events = midiEvents.select({|evt|
		//	(evt[2] == \noteOn) && (evt[5] != 0 )
		//}).sort({|a, b| a[1] < b[1] });

		notes = Dictionary.new;
		onsets = [];

		//events.do({|evt|
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
							note.postln;
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




	crushRange {|degs|

	}


	/*
	findTransposition {
	var notes, note, found, degs, allowed_degrees, find_outside, match, min, lowest,
	outside, headroom, tries, top;

	notes = [];
	found = [];
	allowed_degrees = allowedNotes - allowedNotes[0];

	midiEvents.do({|evt|
	(evt[2] == \noteOn).if ({
	note = evt[4];
	notes.includes(note).not.if({
	notes = notes ++ note;
	})
	})
	});
	notes = notes.sort;

	lowestNote = notes[0];
	degs = notes-lowestNote;
	degrees = degs;

	find_outside = { |degs|

	var notfound, index, flag;

	notfound = [];
	index = 0;
	flag = false;

	"in find_outside".postln;

	degs.do({|d, i|
	flag = false;
	"in loop".postln;
	{ flag.not && (index < allowed_degrees.size) }. while({
	"checking".postln;
	(d == allowed_degrees[index]).if({
	// found!
	flag = true;
	}, { (d > allowed_degrees[index]).if({
	// advance the index
	index = index + 1;
	}, { (d < allowed_degrees[index]).if({
	//check octave transposition
	allowed_degrees.indexOf(d+12).notNil.if({
	flag = true; // off by an octave, but ok
	}, { allowed_degrees.indexOf(d-12).notNil.if({
	flag = true; // off by an octave, but ok
	}, {
	// not found
	notfound = notfound ++ i;
	d.postln;
	(index > 0).if( { index = index - 1}); // back up
	flag = true;
	})})})})})
	})
	});

	notfound.postln;
	notfound = notfound.collect({|i| notes[i] });
	notfound.postln;
	notfound;
	};

	found = [];
	match = false;
	degs = notes-notes[0];
	degs.postln;

	top = allowed_degrees.last;

	tries = 0;

	{(tries < 2) && match.not}.while({

	headroom = top - degs.last;

	{headroom < 0 }.while({
	"range too wide".warn;
	// lower the top octave(s)+ fifth
	degs = degs.collect({|d|
	(d > (top - 7)).if({
	d - 12;
	}, {
	d
	});
	});

	degs = degs.sort;
	headroom = allowed_degrees.last - degs.last;
	});

	{match.not && (degs.last <= allowed_degrees.last)}.while({

	found = found.add(find_outside.(degrees));
	found.last.postln;
	(found.last.size == 0).if ({
	match = true;
	} , {
	degs = degs + 1;
	});
	});

	tries = tries +1;
	top = top -12; // squish by another octave
	});


	match.if({
	"Transposition found!".postln;
	transposition = (found.size -1) + allowedNotes[0]; // this is wrong
	transposition = (found.size -1);// + lowestNote;
	degrees = degs;
	degrees.postln;
	allowed_degrees.postln;
	transposition.postln;
	}, {
	min = inf;

	found.do({|arr, index|
	//min = arr.size.min(min);
	(min > arr.size).if({
	transposition = index;// + allowedNotes[0];
	outside = arr;
	min = arr.size;
	degrees = degrees + transposition;
	})
	});

	outside = outside.collect({|note|
	//	note = note + notes[0] - transposition;
	allNotes[note]
	});

	"Best match has low note % and has % outside notes:\n %".format(allNotes[notes[0]],min, outside).warn;
	found.do({|arr| arr.postln;});
	});

	}
	*/

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
					"transposition % failed".format(tnsp).postln;
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