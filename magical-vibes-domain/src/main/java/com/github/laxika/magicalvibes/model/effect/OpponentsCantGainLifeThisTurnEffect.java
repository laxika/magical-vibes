package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: opponents of the resolving effect's controller can't gain life for the rest
 * of the turn. The affected players are stored in the turn-scoped restriction set on
 * {@code GameData} and cleared at turn cleanup.
 */
public record OpponentsCantGainLifeThisTurnEffect() implements CardEffect {
}
