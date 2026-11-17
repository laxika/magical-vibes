package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * The player whose upkeep it is chooses a color; until end of turn, any land tapped for mana
 * produces mana of that color instead of any other color (Hall of Gemstone). The amount produced is
 * unchanged; only the type is replaced.
 *
 * <p>Resolution pauses for the choice ({@code ChoiceContext.AllLandsProduceChosenColorChoice}); the
 * picked color is kept in a floating effect on every player that lasts until end of turn
 * (the copy of this effect that carries {@link #chosenColor()}), read by
 * {@code GameQueryService.fixedLandManaColor}. For the
 * permanent, fixed-color version use {@link ReplaceLandManaWithColorEffect}; for the
 * land-subtype-scoped version use {@link LandsOfSubtypeProduceFixedManaColorUntilEndOfTurnEffect}.
 */
public record AllLandsProduceChosenColorUntilEndOfTurnEffect(ManaColor chosenColor) implements CardEffect {

    /** The effect as a card has it: the color is chosen as it resolves. */
    public AllLandsProduceChosenColorUntilEndOfTurnEffect() {
        this(null);
    }
}
