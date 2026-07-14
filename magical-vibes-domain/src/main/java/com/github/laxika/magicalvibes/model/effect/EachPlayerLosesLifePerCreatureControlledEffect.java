package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player loses {@code lifePerCreature} life for each creature they control. When
 * {@code attackingOnly} is true, only attacking creatures are counted (e.g. Batwing Brume);
 * otherwise every creature is counted (e.g. Stronghold Discipline).
 */
public record EachPlayerLosesLifePerCreatureControlledEffect(int lifePerCreature, boolean attackingOnly)
        implements CardEffect {
}
