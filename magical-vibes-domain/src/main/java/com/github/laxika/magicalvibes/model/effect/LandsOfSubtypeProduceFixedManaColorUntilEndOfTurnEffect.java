package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Until end of turn, a land of {@code subtype} tapped for mana produces {@code color} instead of any
 * other type (Chaos Moon's even branch, "that Mountain produces colorless mana instead of any other
 * type"). The amount produced is unchanged; only the type is replaced.
 *
 * <p>Recorded in {@code GameData.landSubtypeFixedManaColorThisTurn} and read by
 * {@code GameQueryService.fixedLandManaColor}; cleared by {@code TurnCleanupService}. For the
 * permanent, all-lands version use {@link ReplaceLandManaWithColorEffect}.
 */
public record LandsOfSubtypeProduceFixedManaColorUntilEndOfTurnEffect(CardSubtype subtype, ManaColor color)
        implements CardEffect {
}
