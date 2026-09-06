package com.github.laxika.magicalvibes.model.effect;

/**
 * "Untap all creatures of the creature type of your choice."
 *
 * <p>The controller chooses the creature type during resolution. The choice is held temporarily
 * on {@code GameData.chosenSpellSubtype} while the effect is paused for input.</p>
 */
public record UntapAllCreaturesOfChosenTypeEffect() implements CardEffect {
}
