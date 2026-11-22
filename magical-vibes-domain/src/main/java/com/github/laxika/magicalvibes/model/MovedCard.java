package com.github.laxika.magicalvibes.model;

import java.util.Objects;
import java.util.UUID;

/**
 * The card one object a resolution step acted on became in the zone the step put it in: a permanent destroyed,
 * sacrificed or exiled by the step, as its card in the graveyard or exile. A card moved from one zone to another is a
 * new object (CR 400.7), which the rest of the same effect can still find in the public zone it was moved to
 * (CR 400.7j); this record is how a later step of the resolution finds it ("return each card put into a graveyard this
 * way", "create a token that's a copy of the exiled card").
 *
 * @param objectId the permanent or card the step acted on, as its {@link StepOutcome#objectIds()} lists it
 * @param cardId   the card's id in the zone it went to
 * @param zone     the zone it went to
 * @param ownerId  the player whose graveyard, exile or hand it went to, or {@code null} when not known
 * @param entryVersion the stay in that zone it is expected in, for an ability that reads the step later (a delayed
 *                 triggered ability): a card that left the zone and came back is a new object (CR 603.7c, 400.7);
 *                 {@code -1} for the resolution the step is part of
 */
public record MovedCard(UUID objectId, UUID cardId, Zone zone, UUID ownerId, long entryVersion) {

    public MovedCard {
        Objects.requireNonNull(objectId, "objectId");
        Objects.requireNonNull(cardId, "cardId");
        Objects.requireNonNull(zone, "zone");
    }

    /** The card in whichever stay it is found in, as the resolution that moved it reads it. */
    public MovedCard(UUID objectId, UUID cardId, Zone zone, UUID ownerId) {
        this(objectId, cardId, zone, ownerId, -1);
    }

    /** This card, expected in the stay {@code version} of its zone. */
    public MovedCard expectedInStay(long version) {
        return new MovedCard(objectId, cardId, zone, ownerId, version);
    }
}
