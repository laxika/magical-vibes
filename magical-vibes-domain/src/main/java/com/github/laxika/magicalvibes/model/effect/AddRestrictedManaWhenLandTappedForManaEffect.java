package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * A land-tap triggered mana ability's "its controller may add an additional {C} … spend this mana only …" (Snowfall):
 * the land's controller (as it is on the battlefield, else the player who tapped it) adds {@code amount} mana of {@code color}, or
 * {@code snowAmount} when the tapped land is snow, under {@code restriction}. The "may" is always accepted. Which lands
 * count ("an Island") is the ability's trigger's {@code withFilter}.
 */
public record AddRestrictedManaWhenLandTappedForManaEffect(
        ManaColor color,
        int amount,
        int snowAmount,
        ManaRestriction restriction) implements TriggeredManaAbilityEffect {
}
