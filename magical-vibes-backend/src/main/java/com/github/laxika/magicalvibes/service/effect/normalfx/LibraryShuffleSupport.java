package com.github.laxika.magicalvibes.service.effect.normalfx;

/**
 * Shared shuffle helpers used by every normal Library Shuffle effect handler.
 *
 * <p>Extracted verbatim from the original {@code LibraryShuffleResolutionService} monolith;
 * behavior is identical.
 */
public final class LibraryShuffleSupport {

    private LibraryShuffleSupport() {
    }

    public static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
