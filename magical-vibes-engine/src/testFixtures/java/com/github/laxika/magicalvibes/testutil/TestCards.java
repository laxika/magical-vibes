package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Test-only card helpers.
 */
public final class TestCards {

    private TestCards() {
    }

    /**
     * Test-setup escape hatch for the Card freeze guard: wrapping a card in a {@link Permanent}
     * freezes it (live cards are shared with AI simulation copies and must not be mutated), so
     * tests that tweak a card's stats after creating the permanent would throw. This swaps the
     * permanent's card for an unfrozen {@link Card#createRuntimeCopy()} and returns it, so the
     * tweak mutates a private copy instead of the frozen original.
     *
     * <p>Only for test setup — production code must never mutate a live card.
     */
    public static Card mutableCard(Permanent permanent) {
        Card copy = permanent.getCard().createRuntimeCopy();
        permanent.setCard(copy);
        return copy;
    }
}
