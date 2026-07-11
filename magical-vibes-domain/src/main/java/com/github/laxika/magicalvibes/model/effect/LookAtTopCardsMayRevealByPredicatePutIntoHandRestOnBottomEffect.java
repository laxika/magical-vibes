package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Look at the top {@code count} cards; when {@code anyNumber} is true the controller may reveal
 * up to {@code maxReveal} cards matching {@code predicate} and put them into their hand (otherwise
 * a single card), then put the rest on the bottom of the library. {@code maxReveal} defaults to
 * {@link Integer#MAX_VALUE} ("any number"); pass a smaller value to bound it (e.g. Follow the
 * Lumarets' infusion mode reveals up to two).
 */
public record LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(
        int count,
        CardPredicate predicate,
        boolean anyNumber,
        int maxReveal
) implements CardEffect {

    public LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(int count, CardPredicate predicate) {
        this(count, predicate, false, Integer.MAX_VALUE);
    }

    public LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(int count, CardPredicate predicate, boolean anyNumber) {
        this(count, predicate, anyNumber, Integer.MAX_VALUE);
    }
}
