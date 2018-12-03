/*
D#4 75
G#4 80
A#4 82
C5 84
D#5 87
F5 89
G5 91
G#5 92
A#5 94
C6 96
C#6 97
D#6 99
F6 101
G6 103
G#6 104
A#6 106
*/


(

var note_names, allowed_notes, allowed_degrees, midi, events, notes, degrees, note, find_outside, found, match, min;

note_names = (75: 'D#4',
	80: 'G#4',
	82: 'A#4',
	84: 'C5',
	87: 'D#5',
	89: 'F5',
	91: 'G5',
	92: 'G#5',
	94: 'A#5',
	96: 'C6',
	97: 'C#6',
	99: 'D#6',
	101: 'F6',
	103: 'G6',
	104: 'G#6',
	106: 'A#6');

//allowed_notes = note_names.keys.array;
allowed_notes = [];
note_names.keysDo({|k| allowed_notes = allowed_notes ++ k });
allowed_notes.postln;
allowed_notes = allowed_notes.sort;
allowed_degrees = allowed_notes - allowed_notes[0];

allowed_degrees.postln;

midi = SimpleMIDIFile.read("/home/celesteh/Documents/Gifts/FisherPriceRecords/data/Window_Cleaner.mid");

events = midi.midiEvents;
notes = [];
events.do({|evt|
	(evt[2] == \noteOn).if ({
		note = evt[4];
		notes.includes(note).not.if({
			notes = notes ++ note;
		})
	})
});
notes = notes.sort;
notes.postln;

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
}, {
	min = inf;

	found.do({|arr|
		min = arr.size.min(min);
	});

	"Best match has % outside notes".format(min).postln;
});


)