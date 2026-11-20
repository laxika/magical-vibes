package com.github.laxika.magicalvibes.model;

import java.util.Objects;
import java.util.UUID;

/**
 * A card's power, toughness and mana value as it last existed in the graveyard a resolution step exiled it from
 * (CR 608.2h: the card is no longer in the public zone the effect expected it in). A characteristic-defining power
 * that counts the graveyards, or a change that applies only to graveyard cards, gives a different answer once the card
 * is in exile; "the exiled card's toughness" reads this instead (Morbid Bloom, Scour the Desert, Ritual of the
 * Returned, Soul Separator).
 *
 * @param cardId    the card, as its step's {@link StepOutcome#objectIds()} lists it
 * @param power     its power there; {@code null} for a card without one
 * @param toughness its toughness there; {@code null} for a card without one
 * @param manaValue its mana value there
 */
public record LastKnownCardStats(UUID cardId, Integer power, Integer toughness, int manaValue) {

    public LastKnownCardStats {
        Objects.requireNonNull(cardId, "cardId");
    }
}
