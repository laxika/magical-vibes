package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Capability for a static effect that lets its controller play lands from their graveyard. */
public interface PlayLandsFromGraveyardPermission extends GraveyardPlayPermission {

    /** Optional filter for the lands that may be played; null means any land. */
    default CardPredicate landFilter() {
        return null;
    }
}
