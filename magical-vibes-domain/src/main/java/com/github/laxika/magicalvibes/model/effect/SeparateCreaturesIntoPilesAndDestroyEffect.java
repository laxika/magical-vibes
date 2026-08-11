package com.github.laxika.magicalvibes.model.effect;

/**
 * Separates all creatures target player controls into two piles, then destroys the creatures in
 * the pile that player chooses without allowing regeneration.
 */
public record SeparateCreaturesIntoPilesAndDestroyEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.player()); }
}
