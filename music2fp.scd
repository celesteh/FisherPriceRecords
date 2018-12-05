(
var midi, events, fp;

midi = SimpleMIDIFile.read("/home/celesteh/Documents/Gifts/FisherPriceRecords/data/Window_Cleaner.mid");

events = midi.midiEvents;

fp = FisherPriceRecords(events);
fp.findTransposition

)