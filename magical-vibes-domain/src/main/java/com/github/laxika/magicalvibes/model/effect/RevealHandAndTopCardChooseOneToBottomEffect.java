package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player reveals their hand and the top card of their library; the controller chooses one
 * of those cards to put on the bottom of that player's library.
 */
public record RevealHandAndTopCardChooseOneToBottomEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
