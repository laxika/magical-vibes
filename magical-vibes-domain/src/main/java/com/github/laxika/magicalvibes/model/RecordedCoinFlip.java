package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One logical coin flip already made by a resolving effect that may stop so a player can choose which
 * Krark's Thumb results to ignore. The effect is resolved again and reads these flips back in order
 * instead of flipping anew.
 *
 * @param playerId       the player who flipped the coin, who also chooses its kept result
 * @param heads          the kept result, or {@code null} while the player has not chosen it yet
 * @param physicalFlips  how many coins were physically flipped for this logical flip
 * @param physicalHeads  how many of those physical flips came up heads
 * @param edgarApplied   whether Edgar, King of Figaro made every physical flip come up heads
 * @param pokeyD20Result the d20 Pokey, the Scallywagg rolled instead of flipping, or {@code null}
 */
public record RecordedCoinFlip(UUID playerId, Boolean heads, int physicalFlips, int physicalHeads,
                               boolean edgarApplied, Integer pokeyD20Result) {

    /** Whether the player still has to choose which result to keep. */
    public boolean awaitsChoice() {
        return heads == null;
    }

    public RecordedCoinFlip withHeads(boolean keptHeads) {
        return new RecordedCoinFlip(playerId, keptHeads, physicalFlips, physicalHeads, edgarApplied,
                pokeyD20Result);
    }
}
