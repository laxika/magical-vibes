package com.github.laxika.magicalvibes.model.effect;

/**
 * "Tap any number of untapped creatures you control. You gain {@code lifePerCreature} life
 * for each creature tapped this way."
 *
 * <p>On resolution the controller chooses any subset of their untapped creatures via a
 * multi-permanent choice; each chosen creature is tapped and the controller gains
 * {@code lifePerCreature} life per creature tapped. If the controller has no untapped
 * creatures, nothing happens. Used by Harmony of Nature (lifePerCreature = 4).
 */
public record TapCreaturesGainLifePerCreatureEffect(int lifePerCreature) implements CardEffect {
}
