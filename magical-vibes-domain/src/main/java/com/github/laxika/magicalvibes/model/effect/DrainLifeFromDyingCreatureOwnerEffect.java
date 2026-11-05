package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the owner of the creature that caused an ally-creature-death trigger lose life, then
 * makes the triggered ability's controller gain the same amount. The creature's card is the stack entry's
 * triggering card.
 */
public record DrainLifeFromDyingCreatureOwnerEffect(int amount) implements CardEffect {
}
