(
var midi, events, fp;

midi = SimpleMIDIFile.read("/home/celesteh/Documents/Gifts/FisherPriceRecords/data/bonnieandclyde/noe.mid");

//midi.convertNoteOffs;
midi.timeMode = \seconds;
events = midi.midiEvents;

midi.info;

//midi.length.postln;
//events.last.dump;

fp = FisherPriceRecords(events, "Bonnie and Clyde"//, midi.pathName
);

fp.realise(1, midi.length);

fp.write("/home/celesteh/Documents/Gifts/FisherPriceRecords/data/bonnieandclyde/noe.scad");
//fp.findTransposition

/*
10.do({|i|

	events[events.size - 11 + i].dump;
});


e = events[9];
*/
)