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
    '->  (5) 12345  ->  (6) 12345  ->  (7) 12345  ->  (8) 12345  -.
             --R--          --R--          ---R-          ---R-   |
                                                                  |
    .-------------------------------------------------------------'
    |
    |        ---W-          ----W          W----
    '->  (9) 12345  -> (10) 12345  -> (11) 12345  ->  etc. 
             ----R          ----R          R----

Note that when the Write point overtakes the Read point i.e. fresh data is being written to the loop while the previous data is being played, you can end up with discontinuities and audible clicks in the output.

## controls

_Source section_
* `Input`: the audio device the application reads from (e.g. inbuilt microphone).
* `Output`: the audio device the application plays through (e.g. inbuilt speakers).
* `Refresh` button: If you plug in additional inputs and outputs (say a headset) after starting the application, then pressing 'Refresh' will find the new devices.
* `Restart` button: If you change the input or output, then pressing 'Restart' will reconnect the application to use those instead. 

_Loop sections_
* `Length`: The length of the loop in seconds, minimum length is 0.1s, max is 10s.
* `Speed`: Playback speed from 100% to 10%.
* `Wet mix`: The amount of 'new' sound (i.e. what has come in via the microphone) to add to the loop.
* `Dry mix`: The amount of 'old' sound (i.e. what is already in the loop) to keep as new data is added.
* `Clear` button: Clear out the current loop data.
* `Reverse`: If enabled makes the Read cursor move backwards through the loop.
* `Wet/dry link`: If enabled then moving either the Wet or Dry controls moves the other.
* `Invert wet/dry` button: Switch the wet mix value and dry mix value.

_Level section_
* `Level 1`: Level (volume) to playback the first loop data. This can be set from 0% to 150%.
* `Level 1`: Level (volume) to playback the second loop data. This can be set from 0% to 150%.
* `Mute both` button: Pressing this will mute both tracks. Pressing again will unmute.

_Recording section_
* `Record output`: When recording, write to file the values currently being played via the speaker.
* `Record input`: When recording, write to file the values currently being recorded via the microphone.
* `Recording length`: The length of recording to make, minimum length is 1s, max is 10 minutes.
* `File prefix`: Recordings are created in timestamped files, setting this value adds a prefix to the filename.
* `Clear on record` button: When record is pressed, clear what data is already in both loops.
* `Record` button: Start recording. This will change to a `Cancel` button that allows you to exit recording early.
* `Load properties` button. When recording, the application will save a properties file with the same name as the recording file. This button allows you to reload the settings the application was using at that time.

## notes

* The application creates monophonic wav files.
* If you set 'Dry mix' to 0% and 'Wet mix' to 100%, make some noise and then hit 'Invert wet/dry', it would change 'Dry mix' to 100% and 'Wet mix' to 0% - effectively creating a fixed loop.
* Remember, the Write moves at 'normal' audio speed, it is only the Read that moves slower. If you want to have a very slow play speed for a loop, you may need to disable the `Wet/dry link` and set the `Dry mix` to a higher value so that the loop retains data for longer.
* If you decrease or increase the length of a current loop, it just truncates the data or adds empty space as necessary.
* Switching between `Record output` and `Record input` makes no difference to what you _hear_ playing through the application, only what it records to file.
* The only fields that can't be altered while recording are `File prefix`, `Recording length` and the inbuilt and output devices.
* If you want to, and are very very patient, you can use something like Audacity to find discontinuities from the Read/Write point overlap in the audio and Repair them.
* This is just a stupid _thing_ for playing with, not a commercial application. There might be bugs and I may or may not get round to fixing them. Use it as you wish.
