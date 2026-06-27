package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys each targeted permanent (up to the ability's target count) and returns each card
 * actually put into a graveyard this way to the battlefield under the effect controller's control.
 * Used by Sorin, Lord of Innistrad and similar effects.
 */
public record DestroyUpToTargetsThenReturnFromGraveyardEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
