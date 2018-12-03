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

## How to use

The python script generates a .scad file. Make sure a copy of
fpRecordModule.scad is in the same directory as your new file. Open it with
OpenScad.  Press F6 to render the file. This may take some time. Then save it
as an STL. 
