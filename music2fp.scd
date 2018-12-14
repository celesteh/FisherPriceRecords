(
var midi, events, fp;

midi = SimpleMIDIFile.read(
	"/home/celesteh/Documents/Gifts/FisherPriceRecords/data/zelda/The_Legend_of_Zelda_Main_Theme_Easy-abr.mid"
	//"/home/celesteh/Documents/Gifts/FisherPriceRecords/data/zelda/zelda-oct.mid"
	//"/home/celesteh/Documents/Gifts/FisherPriceRecords/data/rickroll/melody.mid"
	//"/home/celesteh/Documents/Gifts/FisherPriceRecords/data/africa/minmal.mid"
	//"/home/celesteh/Documents/Gifts/FisherPriceRecords/data/wuthering/melody-only.mid"
	//"/home/celesteh/Documents/Gifts/FisherPriceRecords/data/wuthering/three.mid"
	//"/home/celesteh/Documents/Gifts/FisherPriceRecords/data/bonnieandclyde/noe.mid"
);

midi.timeMode = \seconds;
events = midi.midiEvents;

midi.info;

//midi.length.postln;
//events.last.dump;

fp = FisherPriceRecords(events, "Bonnie and Clyde"//, midi.pathName
);

fp.findTransposition().postln;

/*
s.waitForBoot({
	//s.prepareForRecord;
	//s.sync;
	//s.record;
	//s.sync;
	//Pbindf(fp.p,
	//	\db, Pseg(Pseq([0, -3, -5, -9, -15, -20, -500]),
	//		Pseq([10, 20, 10, 270, 60, 10])) + Pwhite(-1.0, 1.0) + 0.2.ampdb,
	//	\legato, Pseg(Pseq([0.8, 1.1, 5, 50, 100, 180, 10]),
	//		Pseq([10, 20, 10, 270, 60, 10]), 'exp'),
	//	\detune, Pwhite(-1.0, 1.0) * Pseg(Pseq([0, 2, 10, 15, 30]), Pseq([100, 100, 100, 80])),
	//	\pan, Pwhite(-1.0, 1.0) * Pseg(Pseq([0, 0.7]), 380)
	//).play;
	fp.p(\wh, midi.length).play
});
*/


//fp.realise(1, midi.length);

//fp.write("/home/celesteh/Documents/Gifts/FisherPriceRecords/data/bonnieandclyde/bonnienclyde2.scad");
//fp.findTransposition


)