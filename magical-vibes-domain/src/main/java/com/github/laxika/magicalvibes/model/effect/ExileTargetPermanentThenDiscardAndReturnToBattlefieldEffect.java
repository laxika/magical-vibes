package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the targeted permanent, returns its card to its owner's hand, then has that player
 * discard that card and put it onto the battlefield from the top of their library.
 */
public record ExileTargetPermanentThenDiscardAndReturnToBattlefieldEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
