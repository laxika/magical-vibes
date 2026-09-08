package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Sacrifice another creature, or deal {@code damage} to the player selected by {@code recipient}.
 * The compact constructor keeps the original source-controller/source-permanent behavior used by
 * Lord of the Pit and Rite of Belzenlok; the enchanted-controller form is used by Aura upkeep
 * triggers such as Unnatural Hunger.
 */
public record SacrificeOtherCreatureOrDamageEffect(DynamicAmount damage, DamageRecipient recipient)
        implements CardEffect {

    public SacrificeOtherCreatureOrDamageEffect(int damage) {
        this(new Fixed(damage), DamageRecipient.CONTROLLER);
    }
}
