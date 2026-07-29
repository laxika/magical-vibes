package com.github.laxika.magicalvibes.model.effect;

/**
 * The player whose upkeep it is chooses a color; until end of turn, any land tapped for mana
 * produces mana of that color instead of any other color (Hall of Gemstone). The amount produced is
 * unchanged; only the type is replaced.
 *
 * <p>Resolution pauses for the choice ({@code ChoiceContext.AllLandsProduceChosenColorChoice}); the
 * picked color is recorded in {@code GameData.allLandsFixedManaColorThisTurn}, read by
 * {@code GameQueryService.fixedLandManaColor} and cleared by {@code TurnCleanupService}. For the
 * permanent, fixed-color version use {@link ReplaceLandManaWithColorEffect}; for the
 * land-subtype-scoped version use {@link LandsOfSubtypeProduceFixedManaColorUntilEndOfTurnEffect}.
 */
public record AllLandsProduceChosenColorUntilEndOfTurnEffect() implements CardEffect {
}
