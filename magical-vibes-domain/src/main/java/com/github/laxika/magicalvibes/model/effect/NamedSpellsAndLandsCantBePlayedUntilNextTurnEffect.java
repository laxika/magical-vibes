package com.github.laxika.magicalvibes.model.effect;

/**
 * A floating effect: spells and lands named {@code cardName} can't be cast or played, by anyone, until the beginning of
 * its controller's next turn.
 *
 * @param cardName the name the controller chose
 */
public record NamedSpellsAndLandsCantBePlayedUntilNextTurnEffect(String cardName) implements CardEffect {
}
