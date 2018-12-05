FisherPriceRecords {

	classvar <allNotes, <allowedNotes, playableDegrees;

	var midiEvents, transposition;

	*initClass {

		var notenames = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];

		allNotes = 127.collect({|i|
			(notenames.wrapAt(i) ++ (i.div(12) + 2)).asSymbol;
		});

		allowedNotes = ["D#4", "G#4", "A#4", "C5", "D#5", "F5", "G#5", "A#5", "C6", "C#6", "D#6", "F6", "G6", "G#6", "A#6"].collect({|note|
			allNotes.indexOf(note.asSymbol);
		});

		playableDegrees = allowedNotes - allowedNotes[0];

	}

	* new{|midiEvents|

		^super.new.init(midiEvents);
	}

	init {|midiArr|

		midiEvents = midiArr;

	}

	findTransposition {
		var notes, note, found, degrees, allowed_degrees, find_outside, match, min,
		outside;

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

		degrees = notes-notes[0];

		find_outside = { |degrees|

			var notfound, index, flag;

			notfound = [];
			index = 0;
			flag = false;

			"in find_outside".postln;

			degrees.do({|d|
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
						// not found
						notfound = notfound ++ d;
						d.postln;
						(index > 0).if( { index = index - 1}); // back up
						flag = true;
					})})})
				})
			});

			notfound.postln;
			notfound;
		};

		found = [];
		match = false;
		degrees = notes-notes[0];
		degrees.postln;

		{match.not && (degrees.last <= allowed_degrees.last)}.while({

			found = found.add(find_outside.(degrees));
			found.last.postln;
			(found.last.size == 0).if ({
				match = true;
			} , {
				degrees = degrees + 1;
			});
		});

		match.if({
			"Transposition found!".postln;
			transposition = (found.size -1) + allowedNotes[0];
		}, {
			min = inf;

			found.do({|arr, index|
				//min = arr.size.min(min);
				(min > arr.size).if({
					transposition = index + allowedNotes[0];
					outside = arr;
					min = arr.size;
				})
			});

			outside = outside.collect({|note|
				note = note + notes[0];
				allNotes[note]
			});

			"Best match has % outside notes: %".format(min, outside).warn;
		});

	}
}