# FisherPriceRecords
Convert a MIDI file to a record compatible with vintage (1970's) Fisher Price music box record players

Forked from [Tycho](https://www.youmagine.com/tycho/designs)'s code at https://www.youmagine.com/designs/fisher-price-record

## Requirements

* [OpenSCAD](http://www.openscad.org/)

### For SuperCollider script

* wslib Quark

### For Python script
* [Music21](http://web.mit.edu/music21/)
* timidity

## SuperCollider Installation

Put the fpclasses into your Extensions directory

## How to use

First, create or modify a MIDI file to play on the music box. Note that not all
notes are not available. The notes you can use are:
D#4, G#4, A#4, C5, D#5, F5, G#5, A#5, C6, C#6, D#6, F6, G6, G#6, A#6

The SuperCollider script will check your file to see if it uses the allowed
notes and if not, will find a transposition if one exists.

First, check the transposition of your file:
```
(
var events;
m = SimpleMIDIFile.read("~/foo.mid");
m.timeMode = \seconds;
events = m.midiEvents;
f = FisherPriceRecords(events, "My Title");
)
```

If no transposition can be found, it will generate a warning and some lists
of outside notes in the transpositions that were the closest fit.

Evaluate the result to see if it sounds ok:
`f.p.play(\foo, m.length)`

Write the scad file:
```
f.realise(1, m.length);
f.write("~/foo.scad");
```

The first argument to `realise` is the number of times the midi file should
repeat on the disk. For very short files, you may want to increase the number.


Make sure a copy of fpRecordModule.scad is in the same directory as your new
file. Open it with OpenScad.  Press F6 to render the file. This may take some
time. Then save it as an STL.
