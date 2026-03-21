package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts target permanent into its owner's library at a specific position from the top.
 * Position is 0-indexed: 0 = top, 1 = second from top, 2 = third from top, etc.
 *
 * @param position the 0-indexed position from the top of the library
 */
public record PutTargetPermanentIntoLibraryNFromTopEffect(int position) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
