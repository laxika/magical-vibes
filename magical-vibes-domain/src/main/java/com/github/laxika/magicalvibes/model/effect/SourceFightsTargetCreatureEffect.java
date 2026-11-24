package com.github.laxika.magicalvibes.model.effect;

/**
 * "This creature fights target creature" (CR 701.14a): the source permanent and the target creature each deal damage
 * equal to its power to the other. Used by Cyclops Gladiator's kin and every "it fights target creature" ability.
 *
 * @param requiresSourceOnBattlefield {@code true} for a fight, which needs both creatures on the battlefield (CR
 *                                    701.14b); {@code false} for "it deals damage equal to its power to target
 *                                    creature. That creature deals damage equal to its power to it", which is no
 *                                    fight: a source that left deals its damage as it last existed and takes none
 * @param chosenAtRandom              whether the target is "chosen at random" ("it fights target creature an
 *                                    opponent controls chosen at random", Scab-Clan Giant): picked among the legal
 *                                    targets as the ability is put on the stack (CR 603.3d), see {@link #atRandom()}
 */
public record SourceFightsTargetCreatureEffect(boolean requiresSourceOnBattlefield, boolean chosenAtRandom)
        implements CardEffect {

    public SourceFightsTargetCreatureEffect() {
        this(true);
    }

    public SourceFightsTargetCreatureEffect(boolean requiresSourceOnBattlefield) {
        this(requiresSourceOnBattlefield, false);
    }

    /** "It fights target creature [the card's filter] chosen at random": the fight with a target chosen at random. */
    public static SourceFightsTargetCreatureEffect atRandom() {
        return new SourceFightsTargetCreatureEffect(true, true);
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec spec = TargetSpec.harmful(TargetPredicates.creature());
        return chosenAtRandom ? spec.atRandom() : spec;
    }
}
