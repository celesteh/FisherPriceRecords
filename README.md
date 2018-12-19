# FisherPriceRecords
Convert a MIDI file to a record compatible with vintage (1970's) Fisher Price music box record players

Forked from [Tycho](https://www.youmagine.com/tycho/designs)'s code at https://www.youmagine.com/designs/fisher-price-record

## Requirements

* [OpenSCAD](http://www.openscad.org/)

### For SuperCollider

* wslib Quark



## How to use

First, create or modify a MIDI file to play on the music box. Note that not all
notes are not available. The notes you can use are:
D#4, G#4, A#4, C5, D#5, F5, G#5, A#5, C6, C#6, D#6, F6, G6, G#6, A#6

The SuperCollider script will check your file to see if it uses the allowed
notes and if not, will find a transposition if one exists.

First, check the transposition of your file:
```
f = FisherPriceRecords("~/foo.mid".standardizePath, "My Title");
```

If no transposition can be found, it will generate a warning and some lists
of outside notes in the transpositions that were the closest fit.

Evaluate the result to see if it sounds ok:
`f.p.play(\foo)`

Write the scad file:
```
f.realise(1);
f.write("~/foo.scad".standardizePath);
```

The argument to `realise` is the number of times the midi file should
repeat on the disk. For very short files, you may want to increase the number.

Be sure fpRecordModule.scad is copied to be in the same directory as
the scad file that you just generated.

Open your scad file in OpenSCAD and see if it looks ok.  The file is
human readable and you can make changes in it and fpRecordModule directly.
You may wish to adjust the pin size and the groove width based on your
printer specifications.

Press F6 to render it. Save it as an STL file. Rendering may take up to half an hour, so be patient.

Follow the manufacturer's instructions if your printer for how to
use the STL file.
