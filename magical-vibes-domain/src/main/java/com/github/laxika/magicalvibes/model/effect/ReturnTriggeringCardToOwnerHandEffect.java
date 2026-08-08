package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death-trigger effect: return the card of the permanent that was put into a graveyard from the
 * battlefield back to a player's hand (fizzles if the card left that graveyard, e.g. it was a
 * token). Used by Enduring Renewal — "Whenever a creature is put into your graveyard from the
 * battlefield, return it to your hand." — and by Yomiji, Who Bars the Way — "Whenever a legendary
 * permanent other than Yomiji is put into a graveyard from the battlefield, return that card to
 * its owner's hand."
 *
 * <p>Card definitions use the no-arg ctor; both fields are bound at trigger time. {@code dyingCardId}
 * is bound via {@link DyingCreatureCardAwareEffect} in the ally-creature-dies path, and the
 * any-permanent-graveyard collector bakes both ids directly.
 *
 * @param dyingCardId the card ID of the permanent that died ({@code null} in the card definition)
 * @param handOwnerId the player whose graveyard is searched and whose hand receives the card;
 *                    {@code null} means the ability controller (Enduring Renewal's "your hand")
 */
public record ReturnTriggeringCardToOwnerHandEffect(UUID dyingCardId, UUID handOwnerId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnTriggeringCardToOwnerHandEffect() {
        this(null, null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnTriggeringCardToOwnerHandEffect(dyingCardId, handOwnerId);
    }
}
