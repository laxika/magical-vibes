package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedule the targeted permanent to be returned to its owner's hand at the beginning of the next
 * end step (e.g. Dragon Mask's "Return it to its owner's hand at the beginning of the next end step").
 */
public record ReturnTargetPermanentToHandAtEndStepEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
