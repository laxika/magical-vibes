package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys each targeted permanent (up to the ability's target count) and returns each card
 * actually put into a graveyard this way to the battlefield under the effect controller's control.
 * Used by Sorin, Lord of Innistrad and similar effects.
 *
 * @param sacrificeAtEndStep whether returned permanents are sacrificed at the beginning of the
 *                           next end step
 */
public record DestroyUpToTargetsThenReturnFromGraveyardEffect(boolean sacrificeAtEndStep) implements CardEffect {

    /** Creates the ordinary destroy-and-return effect without a delayed sacrifice. */
    public DestroyUpToTargetsThenReturnFromGraveyardEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.playerOrPermanent());
    }
}
