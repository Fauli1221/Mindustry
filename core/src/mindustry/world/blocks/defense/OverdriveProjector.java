package mindustry.world.blocks.defense;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class OverdriveProjector extends Block{
    private static final IntSet healed = new IntSet();

    public final int timerUse = timers++;

    public TextureRegion topRegion;
    public float reload = 60f;
    public float range = 80f;
    public float speedBoost = 1.5f;
    public float speedBoostPhase = 0.75f;
    public float useTime = 400f;
    public float phaseRangeBoost = 20f;
    public Color baseColor = Color.valueOf("feb380");
    public Color phaseColor = Color.valueOf("ffd59e");

    public OverdriveProjector(String name){
        super(name);
        solid = true;
        update = true;
        hasPower = true;
        hasItems = true;
        canOverdrive = false;
    }

    @Override
    public boolean outputsItems(){
        return false;
    }

    @Override
    public void load(){
        super.load();
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        Drawf.dashCircle(x * tilesize + offset(), y * tilesize + offset(), range, Pal.accent);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(BlockStat.speedIncrease, (int)(100f * speedBoost), StatUnit.percent);
        stats.add(BlockStat.range, range / tilesize, StatUnit.blocks);

        stats.add(BlockStat.boostEffect, phaseRangeBoost / tilesize, StatUnit.blocks);
        stats.add(BlockStat.boostEffect, (int)((speedBoost + speedBoostPhase) * 100f), StatUnit.percent);
    }

    public class OverdriveEntity extends TileEntity{
        float heat;
        float charge = Mathf.random(reload);
        float phaseHeat;

        @Override
        public void drawLight(){
            Drawf.light(x, y, 50f * efficiency(), baseColor, 0.7f * efficiency());
        }

        @Override
        public void updateTile(){
            heat = Mathf.lerpDelta(heat, consValid() ? 1f : 0f, 0.08f);
            charge += heat * Time.delta();

            phaseHeat = Mathf.lerpDelta(phaseHeat, Mathf.num(cons().optionalValid()), 0.1f);

            if(timer(timerUse, useTime) && efficiency() > 0){
                consume();
            }

            if(charge >= reload){
                float realRange = range + phaseHeat * phaseRangeBoost;
                float realBoost = (speedBoost + phaseHeat * speedBoostPhase) * efficiency();

                charge = 0f;
                indexer.eachBlock(this, realRange, other -> other.timeScale() < realBoost, other -> other.applyBoost(realBoost, reload + 1f));
            }
        }

        @Override
        public void drawSelect(){
            float realRange = range + phaseHeat * phaseRangeBoost;

            Drawf.dashCircle(x, y, realRange, baseColor);
        }

        @Override
        public void draw(){
            super.draw();

            float f = 1f - (Time.time() / 100f) % 1f;

            Draw.color(baseColor, phaseColor, phaseHeat);
            Draw.alpha(heat * Mathf.absin(Time.time(), 10f, 1f) * 0.5f);
            Draw.rect(topRegion, x, y);
            Draw.alpha(1f);
            Lines.stroke((2f * f + 0.2f) * heat);
            Lines.square(x, y, (1f - f) * 8f);

            Draw.reset();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(heat);
            write.f(phaseHeat);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            heat = read.f();
            phaseHeat = read.f();
        }
    }
}
