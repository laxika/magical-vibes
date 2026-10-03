package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Creates a token copy of a card supplied directly by a completed card-choice interaction.
 * A null {@code sourceCard} can instead refer to the card that caused the resolving trigger.
 */
public record CreateTokenCopyOfCardEffect(
        Card sourceCard,
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect
) implements CardEffect {

    /** Creates a token copy of the card that caused the resolving trigger. */
    public static CreateTokenCopyOfCardEffect fromTriggeringCard(
            CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect) {
        return new CreateTokenCopyOfCardEffect(null, tokenCopyEffect);
    }
}
