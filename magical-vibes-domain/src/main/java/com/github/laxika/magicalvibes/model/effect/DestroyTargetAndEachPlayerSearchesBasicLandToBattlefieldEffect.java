package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted permanent. Then each player searches their library for a basic land card,
 * puts it onto the battlefield, then shuffles. By default every player searches, in APNAP order (active player
 * first), as for Field of Ruin.
 *
 * @param targetControllerThenYou when true, only the destroyed permanent's controller and then this ability's
 *                                controller search, in that written order (Demolition Field)
 */
public record DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect(boolean targetControllerThenYou)
        implements RemovalEffect {

    public DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.land());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
