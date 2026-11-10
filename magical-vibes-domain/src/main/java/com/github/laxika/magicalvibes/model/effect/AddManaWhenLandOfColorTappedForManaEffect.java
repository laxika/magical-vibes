package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * A land-tap triggered mana ability's "whenever you tap a land for {C}, add an additional {C}": the ability's controller
 * adds one mana of {@code color} when the tap produced that type, or,
 * for a tap that reported none, when the land could produce it. Who must tap the land is the ability's trigger.
 */
public record AddManaWhenLandOfColorTappedForManaEffect(ManaColor color) implements TriggeredManaAbilityEffect {
}
