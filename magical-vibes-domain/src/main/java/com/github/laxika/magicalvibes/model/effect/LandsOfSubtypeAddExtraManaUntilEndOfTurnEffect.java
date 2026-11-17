package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Until end of turn, whenever any player taps a land of {@code subtype} for mana, that player adds
 * an additional mana of {@code color} (Chaos Moon's odd branch, "whenever a player taps a Mountain
 * for mana, that player adds an additional {R}"). Symmetric — it applies to every player.
 *
 * <p>The resolution makes a floating effect on every player (CR 611.2a) that lasts until end of turn, applied by
 * {@code TapTriggers} (a land tapped for mana).
 * For the permanent, static version use {@link AddManaWhenTappedForManaEffect} on a trigger narrowed to the subtype.
 */
public record LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect(CardSubtype subtype, ManaColor color)
        implements CardEffect {
}
