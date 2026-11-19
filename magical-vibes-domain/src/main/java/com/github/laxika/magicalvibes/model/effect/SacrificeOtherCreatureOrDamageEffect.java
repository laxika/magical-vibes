package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Sacrifice another creature, or deal {@code damage} to the player {@code player} names. {@code you()} (the
 * {@code (int)} constructor) sacrifices a creature other than the source (Lord of the Pit, Rite of Belzenlok); any other
 * subject is "that player" of an Aura's upkeep trigger, who may sacrifice a creature other than the enchanted one
 * (Unnatural Hunger: {@code triggeringPlayer()}).
 */
public record SacrificeOtherCreatureOrDamageEffect(DynamicAmount damage, PlayerSubject player)
        implements CardEffect {

    public SacrificeOtherCreatureOrDamageEffect {
        java.util.Objects.requireNonNull(player, "player");
    }

    public SacrificeOtherCreatureOrDamageEffect(int damage) {
        this(new Fixed(damage), Subjects.you());
    }
}
