package com.Joedobo27.WUmod;


import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ModifiedBy;
import com.wurmonline.server.creatures.Creature;


@SuppressWarnings("unused")
public class ForageJDB
{
    private final String descriptor;
    private final int ordinal;
    private final byte tileType;
    private final GrassData.GrowthStage grassLength;
    private final short category;
    private final int itemType;
    private final byte material;
    private final int chanceAt1;
    private final int chanceAt100;
    private final int difficultyAt1;
    private final int difficultyAt100;
    private final ModifiedBy modifiedBy;
    private final int chanceModifier;

    ForageJDB(final String aDescriptor, final int aOrdinal, final byte aTileType, final GrassData.GrowthStage aGrassLength,
               final short aCategory, final int aItemType, final byte aMaterial, final int aChanceAt1, final int aChanceAt100,
               final int aDifficultyAt1, final int aDifficultyAt100, final ModifiedBy aModifiedBy, final int aChanceModifier) {
        this.descriptor = aDescriptor;
        this.ordinal = aOrdinal;
        this.tileType = aTileType;
        this.grassLength = aGrassLength;
        this.category = aCategory;
        this.itemType = aItemType;
        this.material = aMaterial;
        this.chanceAt1 = aChanceAt1;
        this.chanceAt100 = aChanceAt100;
        this.difficultyAt1 = aDifficultyAt1;
        this.difficultyAt100 = aDifficultyAt100;
        this.modifiedBy = aModifiedBy;
        this.chanceModifier = aChanceModifier;
    }

    private ForageJDB(ForageDataJDB fd) {
        this.descriptor = fd.descriptor;
        this.ordinal = fd.ordinal;
        this.tileType = fd.tileType;
        this.grassLength = fd.grassLength;
        this.category = fd.category;
        this.itemType = fd.itemType;
        this.material = fd.material;
        this.chanceAt1 = fd.chanceAt1;
        this.chanceAt100 = fd.chanceAt100;
        this.difficultyAt1 = fd.difficultyAt1;
        this.difficultyAt100 = fd.difficultyAt100;
        this.modifiedBy = fd.modifiedBy;
        this.chanceModifier = fd.chanceModifier;
    }

    public int getItem() {
        return this.itemType;
    }

    public float getDifficultyAt(final int knowledge) {
        final float diff = this.difficultyAt1 + (this.difficultyAt100 - this.difficultyAt1) / 100 * knowledge;
        if (diff < 0.0f) {
            return knowledge + diff;
        }
        return diff;
    }

    private float getChanceAt(final Creature performer, final int knowledge, final int tilex, final int tiley) {
        final float chance = this.chanceAt1 + (this.chanceAt100 - this.chanceAt1) / 100 * knowledge;
        return chance + this.modifiedBy.chanceModifier(performer, this.chanceModifier, tilex, tiley);
    }

    public byte getMaterial() {
        return this.material;
    }

    public static ForageJDB getRandomForage(Creature performer, byte aTileType, GrassData.GrowthStage aGrassLength, short aCategory, int knowledge, int tileX, int tileY){
        byte checkType = aTileType;
        Tiles.Tile theTile = Tiles.getTile(aTileType);
        if (theTile.isNormalTree()) {
            checkType = Tiles.Tile.TILE_TREE.id;
        }
        if (theTile.isNormalBush()) {
            checkType = Tiles.Tile.TILE_BUSH.id;
        }
        float totalChance = 0.0f;
        for (ForageDataJDB fd : ForageDataJDB.forageEntries) {
            if (fd.tileType == checkType && fd.grassLength == aGrassLength) {
                ForageJDB f = new ForageJDB(fd);
                float chance = f.getChanceAt(performer, knowledge, tileX, tileY);
                if (chance >= 0.0f) {
                    totalChance += chance;
                }
            }
        }
        if (totalChance == 0.0f) {
            return null;
        }
        int rndChance = Server.rand.nextInt((int)totalChance);
        float runningChance = 0.0f;
        for (ForageDataJDB fd : ForageDataJDB.forageEntries) {
            if (fd.tileType == checkType && (checkType != Tiles.Tile.TILE_GRASS.id || fd.grassLength == aGrassLength)) {
                ForageJDB f = new ForageJDB(fd);
                float chance2 = f.getChanceAt(performer, knowledge, tileX, tileY);
                if (chance2 >= 0.0f) {
                    runningChance += chance2;
                    if (rndChance < runningChance) {
                        if (aCategory == 223 || aCategory == f.category) {
                            return f;
                        }
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
