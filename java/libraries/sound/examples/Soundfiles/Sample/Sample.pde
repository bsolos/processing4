/*
This is a sound file player. 
*/


import processing.sound.*;

Sound stream;
SoundFile soundfile;

void setup() {
    size(350,350);
    background(255);
    
    // Create and start the sound renderer
    stream = new Sound(this);
    
    //Load a soundfile
    soundfile = new SoundFile(this, "vibraphon.aiff");

    // These methods return useful infos about the file
    println("SFSampleRate= " + soundfile.sampleRate() + " Hz");
    println("SFSamples= " + soundfile.frames() + " samples");
    println("SFDuration= " + soundfile.duration() + " seconds");

    // Play the file in a loop
    soundfile.loop();
}      


void draw() {
  // Map mouseX from 0.25 to 4.0 for playback rate. 1 equals original playback 
  // speed 2 is an octave up 0.5 is an octave down.
  soundfile.rate(map(mouseX, 0, 350, 0.25, 4.0)); 
  
  // Map mouseY from 0.2 to 1.0 for amplitude  
  soundfile.amp(map(mouseY, 0, 350, 0.2, 1.0)); 
 
  // Map mouseY from -1.0 to 1.0 for left to right 
  soundfile.pan(map(mouseY, 0, 350, -1.0, 1.0));  
}

