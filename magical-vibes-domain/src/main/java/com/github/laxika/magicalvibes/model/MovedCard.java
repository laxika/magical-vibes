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
 */
public record MovedCard(UUID objectId, UUID cardId, Zone zone, UUID ownerId) {

    public MovedCard {
        Objects.requireNonNull(objectId, "objectId");
        Objects.requireNonNull(cardId, "cardId");
        Objects.requireNonNull(zone, "zone");
    }
}
