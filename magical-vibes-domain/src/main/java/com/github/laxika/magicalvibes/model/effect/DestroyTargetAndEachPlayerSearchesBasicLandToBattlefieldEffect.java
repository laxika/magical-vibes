package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted permanent. Then each player searches their library for a basic land card,
 * puts it onto the battlefield, then shuffles. Players search in APNAP order (active player first).
 * The search is mandatory (not "may").
 *
 * <p>Used by Field of Ruin.
 */
public record DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.land());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
