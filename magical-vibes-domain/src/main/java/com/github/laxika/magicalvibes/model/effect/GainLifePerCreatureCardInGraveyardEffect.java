package com.github.laxika.magicalvibes.model.effect;

/**
 * Gains a configurable amount of life for each creature card in the controller's graveyard.
 *
 * @param lifePerCreature amount of life gained per creature card
 */
public record GainLifePerCreatureCardInGraveyardEffect(int lifePerCreature) implements CardEffect {
}
