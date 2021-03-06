package mindustry.world.blocks.distribution;

import arc.math.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

public class BufferedItemBridge extends ExtendingItemBridge{
    public final int timerAccept = timers++;

    public float speed = 40f;
    public int bufferCapacity = 50;

    public BufferedItemBridge(String name){
        super(name);
        hasPower = false;
        hasItems = true;
    }

    public class BufferedItemBridgeEntity extends ExtendingItemBridgeEntity{
        ItemBuffer buffer = new ItemBuffer(bufferCapacity, speed);

        @Override
        public void updateTransport(Tilec other){
            if(buffer.accepts() && items.total() > 0){
                buffer.accept(items.take());
            }

            Item item = buffer.poll();
            if(timer(timerAccept, 4) && item != null && other.acceptItem(this, item)){
                cycleSpeed = Mathf.lerpDelta(cycleSpeed, 4f, 0.05f);
                other.handleItem(this, item);
                buffer.remove();
            }else{
                cycleSpeed = Mathf.lerpDelta(cycleSpeed, 0f, 0.008f);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            buffer.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            buffer.read(read);
        }
    }
}
