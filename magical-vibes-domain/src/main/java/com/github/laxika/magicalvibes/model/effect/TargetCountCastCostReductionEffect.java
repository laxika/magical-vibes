package com.github.laxika.magicalvibes.model.effect;

/** Describes a spell-self generic cost reduction counted once for every chosen target. */
public interface TargetCountCastCostReductionEffect extends CardEffect {

    int amount();
}
