package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Capability for a static effect that lets its controller play lands from their graveyard. */
public interface PlayLandsFromGraveyardPermission extends GraveyardPlayPermission {

    /** Optional filter for the land cards that may be played. */
    default CardPredicate landFilter() {
        return null;
    }
}
