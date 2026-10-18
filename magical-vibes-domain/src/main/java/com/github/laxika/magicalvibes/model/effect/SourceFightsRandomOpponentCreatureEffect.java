package com.github.laxika.magicalvibes.model.effect;

/**
 * The source permanent fights a creature an opponent controls that was chosen uniformly at
 * random as the ability was put onto the stack (CR 701.14a, CR 603.3d). The creature is a real
 * target, so shroud, hexproof and protection remove it from the pool, a creature entering later
 * cannot take its place, and the ability does nothing if the target is illegal on resolution.
 *
 * <p>Used by Scab-Clan Giant ("When this creature enters, it fights target creature an opponent
 * controls chosen at random."). The card declares the target with
 * {@code TargetFilters.creatureAnOpponentControls()}.
 */
public record SourceFightsRandomOpponentCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public boolean targetChosenAtRandom() {
        return true;
    }
}
