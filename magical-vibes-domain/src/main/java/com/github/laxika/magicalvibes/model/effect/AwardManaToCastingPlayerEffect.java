package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Adds mana of the given color to the player who cast the spell that caused the trigger.
 * The casting player is carried by the triggered ability's target context without being a
 * chosen target.
 */
public record AwardManaToCastingPlayerEffect(ManaColor color, DynamicAmount amount) implements CardEffect {

    public AwardManaToCastingPlayerEffect(ManaColor color) {
        this(color, new Fixed(1));
    }

    public AwardManaToCastingPlayerEffect(ManaColor color, int amount) {
        this(color, new Fixed(amount));
    }
}
