// Configuration

$fn = 360; // Roundness. 360 is a absurd high number but the record needs to be very round.

hStock = 1.2; // Height of the flat record
rStock = 59; // Radius of the record
oDrive = 21.8; // offset of the small holes from the center
rDrive = 2; // radius of the four small holes (was 1.65)

hGroove = 2; // Height of the groove (is added to hStock)
pinwidth = 1.6; //  Could be something between 1.3 and 1.6 depending on how much the pins get rounded by the printer.

trackwidth=2; // [1,2] // how many pins should live in one track? 1 or 2?
baseheight = hStock;
grooveheight =hGroove;
ringwidth= 0.46; // Width of the rings that make the groves. Give your nozzle size and exaggerate a bit.


// Usable notes and their corresponding pinnumbers in the musicbox
// System is ["Note", pinnumber], so you could add your own notes if you often call them differently.
notelist = [
          ["D#4", 1],["E-4", 1],["G#4", 2],["A-4", 2],["A#4", 3],["B-4", 3],
["C5", 4],["D#5", 5],["E-5", 5],["F5", 6],["G5", 7],["G#5", 8],["A-5", 8],["G#5_2", 9],
        ["A-5_2", 9],["A#5", 10],["B-5", 10],["A#5_2", 11],["B-5_2", 11],
["C6", 12],["C6_2", 13],["C#6", 14],["D-6", 14],["C#6_2", 15],["D-6_2", 15],["D#6", 16],["E-6", 16],["D#6_2", 17],["E-6_2", 17],["F6", 18],["F6_2", 19],["G6", 20],["G#6", 21],["A-6", 21],["A#6", 22],["B-6",22]];

// a composition is written as:
// [  [["note", "note"], count ],[["note"], count ]
// Count is the rotationstep. The heighest count is taken to calculate the length of the piece. 
// If you have a very short piece you can put as the last rotationstep a higher number to speedup the tempo: [[], 20]
// Some notes come double in the musicbox. This allows to play the same note quickly after another.
// A series of short notes can be written as: [ ["A-5"], 2.0 ],[["A-5_2"], 2.25],[["A-5"], 2.5]
// Notes that not exist in the above list will not be rendered and you get a warning in the console. 

// An example of a composition. This variable is not used when createMusic is called from another file and a variable 'composition' is given there.
composition = [ 
    // All pins
    
    [["D#4"], 18],
    [["G#4"], 19],
    [["A#4"], 20],
    [["C5"], 21],
    [["D#5"], 22],
    [["F5"], 23],
    [["G5"], 24],
    [["G#5"], 25],
    [["G#5_2"], 26],
    [["A#5"], 27],
    [["A#5_2"], 28],
    [["C6"], 29],
    [["C6_2"], 30],
    [["C#6"], 31],
    [["C#6_2"], 32],
    [["D#6"], 33],
    [["D#6_2"], 34],
    [["F6"], 35],
    [["F6_2"], 36],
    [["G6"], 37],
    [["G#6"], 38],
    [["A#6"], 39],

    // All individual notes (if I am correct)
    [["D#4"],0],
    [["G#4"],1],
    [["A#4"],2],
    [["C5"],3],
    [["D#5"],4],
    [["F5"],5],
    [["G#5"],6],
    [["A#5"],7],
    [["C6"],8],
    [["C#6"],9],
    [["D#6"],10],
    [["F6"],11],
    [["G6"],12],
    [["G#6"],13],
    [["A#6"],14],
    [[], 42 ]]; // A little rest
              

module createBlank() {
	difference() {
		// stock
		circle(r=rStock, center=true);
		// Centre hole
		circle(r=3.75, center=true);
		// Drive holes
		for(i=[0:90:360]){rotate(i)translate([0,oDrive,0]) { circle(r=rDrive, center=true); }
    
        }
    }
}
module track(outer_radius, width=1){
    difference(){
        circle(r=outer_radius);
        circle(r=outer_radius-width);
    }
}

module pin(angle, row){
    note = row * 1.39;
    rotate([0,0,angle]){
        // the pin is first moved out from the middle plus moved according to the note. The first movement is 1mm less than the first groovewall. This seems to be a nice place the stay within the rounding of the groove.
    translate([26.5+note,0,0]){
        // The actual pin [width, length]. A length of 2+ gives a better visual indication of pins to close to each other but pins of 1.2 look more like te original records.
        square([pinwidth,1.2], center=true);
    }
}
}

module createMusic(){
    
    musicsteps = max([for(i = composition) i[1]]);
    rotationfactor = 360/musicsteps;
    
    
        // Create 22 tracks, double second number for 11 tracks
        // 1.39 or 2.78
        for (a =[27.5:1.39*trackwidth:59]) track(a, ringwidth);
       
            
        // Layout the pins
        for(chord = composition, i = search(chord[0], notelist)){
            echo(str("Notenumber: ", notelist[i][1], 
                    " Notename: ", notelist[i][0], 
                    " Position: ", chord[1] ));
            if (notelist[i])    // If there is a tuple with notenumber and pinnumber
                pin(chord[1]*rotationfactor, notelist[i][1]);
            else                // The note is not in de notelist, so not playable
                echo(str("<b>Warning!</b> Unknown note in: ", chord ));
       }
    
};

module writeText(title,y=5,size=8, font=":style=Italic"){
    linear_extrude(height=hStock+1){
        translate([0,y,0]){
            text(title, halign="center", font=font, size=size);
        }
    }
}

//    union(){
//        intersection(){
//            difference(){
//                cube([150,10,10]);
//                cube([50,6,25], center=true);
//            }

union(){
        linear_extrude(height=hStock){
            createBlank();
        }
        
        linear_extrude(height=hStock+hGroove){
            createMusic();
       
        }
        linear_extrude(height=hStock+hGroove+0.4){ // Lift the side 0.4 mm
          track(27.6-ringwidth,0.9);
          track(59.8,1.4);
        }
                
}

//
//}}