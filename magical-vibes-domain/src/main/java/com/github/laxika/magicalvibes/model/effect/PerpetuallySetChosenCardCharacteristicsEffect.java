package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies perpetual color and mana-cost characteristics to the card selected by a hand choice. */
public record PerpetuallySetChosenCardCharacteristicsEffect(CardColor color, String manaCost)
        implements CardEffect {
}
