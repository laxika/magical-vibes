package com.github.laxika.magicalvibes.model;

/** Search details held while the bottom-card reverse-miracle choice is answered. */
public record PendingReverseMiracleSearch(
        LibrarySearchParams params,
        String messagePrompt,
        boolean messageCanFailToFind
) {
}
