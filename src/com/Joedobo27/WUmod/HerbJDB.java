package com.Joedobo27.WUmod;


import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ModifiedBy;
import com.wurmonline.server.creatures.Creature;

@SuppressWarnings("unused")
public class HerbJDB {
    private String descriptor;
    private int ordinal;
    private byte tileType;
    private GrassData.GrowthStage grassLength;
    private short category;
    private int itemType;
    private byte material;
    private int chanceAt1;
    private int chanceAt100;
    private int difficultyAt1;
    private int difficultyAt100;
    private ModifiedBy modifiedBy;
    private int chanceModifier;

    private HerbJDB(final String aDescriptor, final int aOrdinal, final byte aTileType, final GrassData.GrowthStage aGrassLength, final short aCategory, final int aItemType, final byte aMaterial, final int aChanceAt1, final int aChanceAt100, final int aDifficultyAt1, final int aDifficultyAt100, final ModifiedBy aModifiedBy, final int aChanceModifier) {
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

    private HerbJDB(HerbDataJDB hd) {
        this.descriptor = hd.descriptor;
        this.ordinal = hd.ordinal;
        this.tileType = hd.tileType;
        this.grassLength = hd.grassLength;
        this.category = hd.category;
        this.itemType = hd.itemType;
        this.material = hd.material;
        this.chanceAt1 = hd.chanceAt1;
        this.chanceAt100 = hd.chanceAt100;
        this.difficultyAt1 = hd.difficultyAt1;
        this.difficultyAt100 = hd.difficultyAt100;
        this.modifiedBy = hd.modifiedBy;
        this.chanceModifier = hd.chanceModifier;
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

    public static HerbJDB getRandomHerb(final Creature performer, final byte aTileType, final GrassData.GrowthStage aGrassLength, final short aCategory, final int knowledge, final int tileX, final int tileY) {
        byte checkType = aTileType;
        final Tiles.Tile theTile = Tiles.getTile(aTileType);
        if (theTile.isNormalTree()) {
            checkType = Tiles.Tile.TILE_TREE.id;
        }
        if (theTile.isNormalBush()) {
            checkType = Tiles.Tile.TILE_BUSH.id;
        }
        float totalChance = 0.0f;

        for (HerbDataJDB hd : HerbDataJDB.herbEntries) {
            if (hd.tileType == checkType && hd.grassLength == aGrassLength) {
                HerbJDB h = new HerbJDB(hd);
                float chance = h.getChanceAt(performer, knowledge, tileX, tileY);
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
        for (HerbDataJDB hd : HerbDataJDB.herbEntries) {
            if (hd.tileType == checkType && (checkType != Tiles.Tile.TILE_GRASS.id || hd.grassLength == aGrassLength)) {
                HerbJDB h = new HerbJDB(hd);
                float chance2 = h.getChanceAt(performer, knowledge, tileX, tileY);
                if (chance2 >= 0.0f) {
                    runningChance += chance2;
                    if (rndChance < runningChance) {
                        if (aCategory == 224 || aCategory == h.category) {
                            return h;
                        }
                        return null;
                    }
                }
            }
        }
        return null;
    }
}


