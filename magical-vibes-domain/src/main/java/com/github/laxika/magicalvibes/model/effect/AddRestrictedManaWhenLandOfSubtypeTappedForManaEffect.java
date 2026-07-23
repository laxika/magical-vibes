package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Land-tap trigger: whenever any land of the given subtype is tapped for mana, its controller
 * adds {@code amount} additional mana of {@code color} under {@code restriction}. If that land
 * is snow, adds {@code snowAmount} instead. Symmetric — affects every player's matching lands.
 * "May" is auto-accepted (Sanctimony-style). Used by Snowfall (Island → {U}/{U}{U} CU-only).
 */
public record AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect(
        CardSubtype subtype,
        ManaColor color,
        int amount,
        int snowAmount,
        ManaRestriction restriction) implements CardEffect {
}
