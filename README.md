# slowloop

Can you recreate 'I Am Sitting In A Room' by Alvin Lucier using the inbuilt microphone and speakers of a laptop?
No, not really, the quality of those devices leads to terrible screech as the higher frequencies survive each iteration.
This _thing_ attempts to get round that problem by slowing the playback of what has been recorded, to reduce the higher frequencies. 

## how it works

In a given loop there are two cursors, the point where the next frame of data is being written, and the point which is used for playback. These cursors move at different speed - where the read point moves slower according to the setting

For example, if the (R)ead point is moving at half speed of the (W)rite point, and there were only four frames of data, you could consider the cursors to move like this:

`

              W---           -W--           --W-           ---W
         (1)  1234  ->  (2)  1234  ->  (3)  1234  ->  (4)  1234  -.
              R---           R---           -R--           -R--   |
                                                                  |
    .-------------------------------------------------------------'
    |
    |         W---           -W--           --W-           ---W
    '->  (5)  1234  ->  (6)  1234  ->  (7)  1234  ->  (8)  1234  -.
              --R-           --R-           ---R           ---R   |
                                                                  |
    .-------------------------------------------------------------'
    |
    |         W---           -W--           --W-
    '->  (9)  1234  ->  (10) 1234  ->  (11) 1234  ->  etc. 
              R---           R---           -R--

`

## recording


## notes

