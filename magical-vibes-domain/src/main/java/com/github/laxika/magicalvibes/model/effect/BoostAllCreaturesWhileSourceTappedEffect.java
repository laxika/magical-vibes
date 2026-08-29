package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives all creatures a fixed boost for as long as the source permanent remains tapped.
 * The normal-effect handler snapshots the affected creatures when the ability resolves and
 * records one source-linked floating boost for each creature.
 */
public record BoostAllCreaturesWhileSourceTappedEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
