# slowloop

Can you recreate 'I Am Sitting In A Room' by Alvin Lucier using the inbuilt microphone and speakers of a laptop?
No, not really, the quality of those devices leads to terrible screech as the higher frequencies survive each iteration.
This _thing_ attempts to get round that problem by slowing the playback of what has been recorded, to reduce the higher frequencies. 

## how it works

The application has two loops which it writes to and reads from simultaneously. In a given loop there are two cursors, the point where the next frame of data is being written, and the point which is used for playback. These cursors move at different speed - where the read point moves slower according to the setting

For example, if the (R)ead point is moving at half speed of the (W)rite point, and there were only five frames of data, you could consider the cursors to move like this:


             W----          -W---          --W--          ---W-
         (1) 12345  ->  (2) 12345  ->  (3) 12345  ->  (4) 12345  -.
             R----          R----          -R---          -R---   |
                                                                  |
    .-------------------------------------------------------------'
    |
    |        ----W          W----          -W---          --W--
    '->  (5) 12345  ->  (6) 12345  ->  (7) 12345  ->  (8) 1234   -.
             --R--          --R--          ---R-          ---R-   |
                                                                  |
    .-------------------------------------------------------------'
    |
    |        ---W-          ----W          W----
    '->  (9) 12345  -> (10) 12345  -> (11) 12345  ->  etc. 
             ----R          ----R          R----

Note that when the Write point overtakes the Read point i.e. fresh data is being written to the loop while the previous data is being played, you can end up with discontinuities and audible clicks in the output.

## controls

_Input section_

On startup the application will use the first input and output devices it finds (say inbuilt microphone and speakers). If you have alternative inputs and outputs they can be chosen from the dropdowns - and pressing 'Restart' will reconnect the application to use those instead. If you plug in additional inputs and outputs (say a headset) after starting the application, then pressing 'Refresh' will find the new devices

_Loop sections_

* `Length`: the length of the loop in seconds, minimum length is 0.1s, max is 10s.
* `Speed`: playback speed from 100% to 10%.
* `Wet mix`: the amount of 'new' sound (i.e. what has come in via the microphone) to add to the loop
* `Dry mix`: the amount of 'old' sound (i.e. what is already in the loop) to keep as new data is added
* `Clear button`: clear out the current loop data
* `Reverse`: if enabled makes the Read cursor move backwards through the loop
* `Wet/dry link`: if enabled then moving either the Wet or Dry controls moves the other
* `Invert wet/dry`: switch the wet mix value and dry mix value

_Level section_
* `Level 1`: level (volume) to playback the first loop data. This can be set from 0% to 150%
* `Level 1`: level (volume) to playback the second loop data. This can be set from 0% to 150%
* `Mute both`: pressing this will mute both tracks. Pressing again will unmute.

_Recording section_
* `Record output`: when recording, write the values currently being played via the speaker
* `Record input`: when recording, write the values currently being recorded via the microphone

## notes

