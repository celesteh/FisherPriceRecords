#!/usr/bin/env python3
from datetime import datetime
import argparse
import music21 as m
m.environment.set('midiPath', '/usr/bin/timidity')
m.environment.set('musicxmlPath','/usr/bin/musescore')
#m.environment.set('musicxmlPath', '/usr/bin/nted')
# m.environment.set('midiPath', '/usr/bin/cvlc')

warning_count=0

parser = argparse.ArgumentParser(description='Convert musicfiles to FisherPrice Musicbox records.')
# parser.add_argument('musicfile', type=argparse.FileType('r'), default=sys.stdin, help="input file (midi, ABC, MusicXML, etc.)")
parser.add_argument('filename', type=str, help="input file (midi, ABC, MusicXML, etc.)")
parser.add_argument('-t', '--transposition', type=int, default='0', help="halfnotes to transpose")
parser.add_argument('-m', '--midi', action="store_true", default='0', help="output a midi file (bug: produces only full notes)")
args = parser.parse_args()
musicfile = args.filename
filename =args.filename.split('.')
transposition = args.transposition
musicstream = m.converter.parse(musicfile).flat.transpose(transposition, inPlace=False)
outputmusic = m.stream.Stream()
# Tuple of legal notenames:
legalnotelist = ("D#4", "E-4", "G#4", "A-4", "A#4", "B-4", "C5", "D#5", "E-5", "F5", "G5", "G#5", "A-5", "G#5_2",
                 "A-5_2", "A#5", "B-5", "A#5_2", "B-5_2", "C6", "C6_2", "C#6", "D-6", "C#6_2", "D-6_2", "D#6", "E-6",
                 "D#6_2", "E-6_2", "F6", "F6_2", "G6", "G#6", "A-6", "A#6", "B-6", )

header=('''// OpenSCAD file - http://www.openscad.org
// converted from %s by music2fp.py
//
include <fpRecordModule.scad>

// Notes you can use:
// notes with a "_2" are double and can also be written without the "_2".
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
''' % musicfile)
footer =('''

writeText("%s", y=8, size=6);
writeText("%s", y=-10, size=4);


// That's all!''' % (filename[0], datetime.now().strftime('%x (%X)')))

print(musicstream)

def writenote(notes, offset,file):
    count = 0
    for anote in notes:
        if anote not in legalnotelist:
                print("Note %s is disregarded later." % anote)
                print("WARNING! This is not going to sound nice")
                count = count + 1
        else:
            newnote = m.note.Note()
            newnote.nameWithOctave = anote
            newnote.offset = offset # does not take into account the note length
            outputmusic.append(newnote)

    line=("[%s, %s]," % (notes, offset)).replace('\'','\"')
    print(line, file=file)
    return count


with open(filename[0]+'.scad', 'wt') as f:
    print(header, file=f)
    print('composition = [ ', file=f)
    for m21object in musicstream:
        print(m21object)
        if type(m21object) == m.note.Note:
            warning_count = warning_count + writenote([m21object.nameWithOctave], m21object.offset, f)
        elif type(m21object) == m.chord.Chord:
            notesinchord=[]
            for anote in m21object:
                notesinchord.append(anote.nameWithOctave)
            warning_count = warning_count + writenote(notesinchord, m21object.offset, f)
    print('];', file=f)
    print(footer, file=f)

if args.midi:
    outputmusic.write('midi', filename[0]+"_out.mid")

print ("Warnings: \n", warning_count)
