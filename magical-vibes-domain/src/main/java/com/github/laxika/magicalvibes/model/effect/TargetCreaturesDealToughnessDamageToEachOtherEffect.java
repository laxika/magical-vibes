package com.github.laxika.magicalvibes.model.effect;

/**
 * The creatures chosen for two target groups deal damage to each other equal to their respective
 * toughnesses. This is distinct from a fight, which uses power.
 */
public record TargetCreaturesDealToughnessDamageToEachOtherEffect(int firstTargetGroup,
                                                                   int secondTargetGroup)
        implements CardEffect {

    /** Uses the first two target groups. */
    public TargetCreaturesDealToughnessDamageToEachOtherEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}
