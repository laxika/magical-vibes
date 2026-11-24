package com.github.laxika.magicalvibes.model.effect;

/**
 * Hall of Gemstone: the player whose upkeep it is chooses a color; until end of turn, lands tapped for mana
 * produce mana of the chosen color instead of any other color, so colorless mana stays.
 *
 * <p>Resolution pauses for the choice ({@code ChoiceContext.AllLandsProduceChosenColorChoice}); the answer floats
 * a {@link ReplaceLandManaWithColorEffect} of that color replacing every color, until end of turn.
 */
public record AllLandsProduceChosenColorUntilEndOfTurnEffect() implements CardEffect {
}
