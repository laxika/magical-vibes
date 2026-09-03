package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "Choose a creature type. All creatures of that type get +P/+T until end of turn."
 *
 * <p>The creature type is chosen during resolution and stored temporarily on
 * {@code GameData.chosenSpellSubtype}; the effect then applies a one-shot modifier to every
 * matching creature on the battlefield. The amounts are evaluated once after the creature type
 * is chosen.</p>
 */
public record BoostAllCreaturesOfChosenSubtypeEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost)
        implements CardEffect {

    public BoostAllCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }
}
