package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * When resolved, every land on the battlefield becomes the given basic land type until end of
 * turn, replacing its other land types and mana ability.
 */
public record AllLandsBecomeBasicLandTypeUntilEndOfTurnEffect(CardSubtype subtype) implements CardEffect {
}
