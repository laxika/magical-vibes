package com.github.laxika.magicalvibes.model.effect;

/**
 * As an additional cost to cast this spell, you may sacrifice any number of creatures.
 * This spell costs {@code reductionPerCreature} less to cast for each creature sacrificed this way.
 */
public record SacrificeCreaturesForCostReductionEffect(int reductionPerCreature) implements CardEffect {
}
