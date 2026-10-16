package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * When the source permanent dies, put the imprinted (exiled) card into its owner's hand.
 * Used by cards like Hoarding Dragon.
 *
 * @param boundCardId the exiled card this ability refers to, fixed when the ability triggers so a
 *                    later change to the source's imprint (the Dragon returning and exiling a
 *                    different card) cannot redirect it; {@code null} until bound
 */
public record PutImprintedCardIntoOwnersHandEffect(UUID boundCardId) implements CardEffect {

    public PutImprintedCardIntoOwnersHandEffect() {
        this(null);
    }
}
