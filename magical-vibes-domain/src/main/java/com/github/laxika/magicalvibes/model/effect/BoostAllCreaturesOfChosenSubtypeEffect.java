package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type. All creatures of that type get +P/+T until end of turn."
 *
 * <p>The creature type is chosen during resolution and stored temporarily on
 * {@code GameData.chosenSpellSubtype}; the effect then applies a one-shot modifier to every
 * matching creature on the battlefield.</p>
 */
public record BoostAllCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
