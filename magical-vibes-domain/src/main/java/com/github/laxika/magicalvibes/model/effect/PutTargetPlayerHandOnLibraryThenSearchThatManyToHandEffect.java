package com.github.laxika.magicalvibes.model.effect;

/**
 * Jester's Mask: the target player puts the cards from their hand on top of their library, then
 * the controller searches that player's library for that many cards; the target player puts those
 * cards into their hand and shuffles.
 *
 * <p>Because the library is shuffled at the end of the effect, the position and order of the
 * returned hand cards within the library is unobservable — the handler simply merges the hand into
 * the library and drives a mandatory {@code handSize}-card search whose destination is the target
 * player's hand.
 */
public record PutTargetPlayerHandOnLibraryThenSearchThatManyToHandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
