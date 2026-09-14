package com.github.laxika.magicalvibes.model.effect;

/** Describes a battlefield tax counted once for every target of an opponent's spell. */
public interface PerTargetCastCostIncreaseEffect extends CardEffect {

    int amount();
}
