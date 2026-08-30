package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Creates a token copy of a card supplied directly by a completed card-choice interaction. */
public record CreateTokenCopyOfCardEffect(
        Card sourceCard,
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect
) implements CardEffect {
}
