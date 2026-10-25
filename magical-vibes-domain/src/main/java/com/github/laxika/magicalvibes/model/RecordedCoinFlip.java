package com.github.laxika.magicalvibes.model;

/**
 * One logical coin flip already made by a resolving effect that stopped so its player could choose
 * which Krark's Thumb results to ignore. The effect is resolved again and reads these flips back in
 * order instead of flipping anew.
 *
 * @param heads         the kept result, or {@code null} while the player has not chosen it yet
 * @param physicalFlips how many coins were physically flipped for this logical flip
 * @param physicalHeads how many of those physical flips came up heads
 * @param edgarApplied  whether Edgar, King of Figaro made every physical flip come up heads
 */
public record RecordedCoinFlip(Boolean heads, int physicalFlips, int physicalHeads, boolean edgarApplied) {

    /** Whether the player still has to choose which result to keep. */
    public boolean awaitsChoice() {
        return heads == null;
    }

    public RecordedCoinFlip withHeads(boolean keptHeads) {
        return new RecordedCoinFlip(keptHeads, physicalFlips, physicalHeads, edgarApplied);
    }
}
