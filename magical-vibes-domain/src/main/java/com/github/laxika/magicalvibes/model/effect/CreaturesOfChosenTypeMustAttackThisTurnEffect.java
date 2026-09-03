package com.github.laxika.magicalvibes.model.effect;

/**
 * "Creatures of the creature type of your choice attack this turn if able."
 *
 * <p>The controller chooses the creature type during resolution. The choice is held temporarily
 * on {@code GameData.chosenSpellSubtype} while the effect is paused for input.</p>
 */
public record CreaturesOfChosenTypeMustAttackThisTurnEffect() implements CardEffect {
}
