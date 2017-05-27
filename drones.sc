s.options.numBuffers = 16000;
s.options.memSize = 655360;
s.boot;
s.freqscope;
s.plotTree;
s.scope(2);

// G, A, B, C, D, E, and F♯

//Scale.major.degreeToFreq((0..7), (60-5).midicps, 0);
~notes = Scale.major.degreeToFreq((0..(8*7)), 31.midicps, 0);
~gs = Scale.major.degreeToFreq((0..8)*7, 31.midicps, 0);
~gsm = ~gs.cpsmidi;
~gsmroot = ~gsm - 60;

//
//    I – G major, G major seventh (Gmaj, Gmaj7)
//    ii – A minor, A minor seventh (Am, Am7)
//    iii – B minor, B minor seventh (Bm, Bm7)
//    IV – C major, C major seventh (C, Cmaj 7)
//    V – D major, D dominant seventh (D, D7)
//    vi – E minor, E minor seventh (Em, Em7)
//    vii – F# diminished, F# minor seventh flat five (F#°, F#m7b5)
// Common chord progressions in G major
// I - IV - V 	    G - C - D
// I - vi - IV - V 	G - Em - C - D
// ii - V - I 	    Am - D7 - GM7
~gmaj   = [0, 2, 4]; //G B D
~gmaj7  = [0, 2, 4, 6]; //G B D F#
~amin   = [1,3,5]; // A C E
~bmin   = [2,4,6];//B D F#
~bmin7  = [2,4,6,8];//B D F# A
~cmaj   = [-4,-2,0]; //C E G
~cmaj7  = [-4,-2,0,2]; //C E G
~dmaj   = [-3,-1,1];//DF#A
~dmaj7  = [-3,-1,1,3];//DF#AC
~emin   = [-2,0,2];//EGB
~emin7  = [-2,0,2,4];//EGBD
~fsdim  = [-1,1,3];//F#AC
~fsdim7 = [-1,1,3,5];//F#ACE

~chords = [~gmaj,~amin,~bmin,~cmaj,~dmaj,~emin,~fsdim];
~chords7 = [~gmaj7,~bmin7,~cmaj7,~dmaj7,~emin7,~fsdim7];
~allchords = [~chords,~chords7].lace(); // zip

// I - vi - IV - V 	G - Em - C - D
// ii - V - I 	    Am - D7 - GM7
//
~progI = [~gmaj,~cmaj,~dmaj];
~progII = [~gmaj,~emin,~cmaj,~dmaj];




SynthDef(\drone, { |out, freq = 440, gate = 0.5, amp = 1.0, attack = 0.04, release=0.1 |
	var sig,nsize,n = (2..20);
	nsize = n.size;
	sig = ((
		n.collect {arg i; 
			SinOsc.ar( (1.0 - (1.0/(i*i))) * freq )
		}).sum / nsize)
	* EnvGen.kr(Env.adsr(attack, 0.2, 0.6, release), gate, doneAction:2)
	* amp;
    Out.ar(out, sig ! 2)
}).add;

/*
	Pbind(
		\instrument,\drone,
		\dur, Pstutter(4,Pshuf([0.1,0.2,0.4,0.5,1.0,4.0],inf),inf),
		\degree, Pshuf([Pseq(~progI),Pseq(~progII)], inf), // your melody goes here
		\scale, Scale.major, // your scale goes here
		\root, Pstutter(4,Pshuf(~gsmroot[(0..(~gsmroot.size / 2))],inf),inf), // semitones relative to 60.midicps, so this is G
//\attack, 0.0,
//		\release, 1.0
	).play;
*/

//x = Synth(\drone, 
//	[\out,0, \freq,440,\gate,0.5,\amp,0.1,\attack,0.04,\release,5.0])
//x.set(\gate,0)
// x.set(\amp,0.1)
// x.set(\gate,1)
// x.set(\release,10.0)

MIDIClient.init;
MIDIIn.connectAll;

~notes = Array.newClear(128);    // array has one slot per possible MIDI note

~on = MIDIFunc.noteOn({ |veloc, num, chan, src|
	[veloc,num,chan,src].postln;
	if(chan==0,{
		if(nil!=~notes[num],{
			~notes[num].release;
		});
		~notes[num] = Synth(\drone, 
			[\out,0, \freq,num.midicps,\gate,0.5,\amp,0.5 * veloc * 0.00315,\attack,0.04,\release,5.0])
	})
});

~off = MIDIFunc.noteOff({ |veloc, num, chan, src|
	if(chan==0,{
		~notes[num].release;
		~notes[num] = nil;
	});
});

// ~notes.do {|x| x.free; }

