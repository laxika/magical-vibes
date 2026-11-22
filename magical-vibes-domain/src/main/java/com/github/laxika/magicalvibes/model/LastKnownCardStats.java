package com.github.laxika.magicalvibes.model;

import java.util.Objects;
import java.util.UUID;

/**
 * A card's power, toughness and mana value as it last existed in the graveyard a resolution step exiled it from
 * (CR 608.2h: the card is no longer in the public zone the effect expected it in). A characteristic-defining power
 * that counts the graveyards, or a change that applies only to graveyard cards, gives a different answer once the card
 * is in exile; "the exiled card's toughness" reads this instead (Morbid Bloom, Scour the Desert, Ritual of the
 * Returned, Soul Separator). A permanent the step exiled is recorded the same way, as it last existed on the
 * battlefield ({@link StepOutcome#lastKnownPermanents()}).
 *
 * @param cardId    the card, or the permanent, as its step's {@link StepOutcome#objectIds()} lists it
 * @param power     its power there; {@code null} for a card without one
 * @param toughness its toughness there; {@code null} for a card without one
 * @param manaValue its mana value there
 * @param name      an exiled permanent's name as it last existed on the battlefield, {@code null} for one with no name
 *                  (face down, CR 708.2) and for a card, which keeps its name
 * @param controllerId an exiled permanent's controller as it last existed on the battlefield ("creatures they
 *                  controlled that were exiled this way"); {@code null} for a card
 */
public record LastKnownCardStats(UUID cardId, Integer power, Integer toughness, int manaValue, String name,
                                 UUID controllerId) {

    /** A card, whose name is read from the card. */
    public LastKnownCardStats(UUID cardId, Integer power, Integer toughness, int manaValue) {
        this(cardId, power, toughness, manaValue, null, null);
    }

    public LastKnownCardStats {
        Objects.requireNonNull(cardId, "cardId");
    }
}
