package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent returns a nonland permanent with the greatest mana value among permanents they
 * control to its owner's hand, then discards a card. Opponents are processed in APNAP order and
 * choose among tied permanents.
 */
public record EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffect()
        implements CardEffect {
}
