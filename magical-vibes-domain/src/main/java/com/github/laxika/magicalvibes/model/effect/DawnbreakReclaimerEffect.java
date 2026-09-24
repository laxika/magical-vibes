package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import java.util.UUID;

/** Dawnbreak Reclaimer's sequential graveyard choices and optional return. */
public record DawnbreakReclaimerEffect(CardPredicate filter, UUID opponentCardId, UUID ownCardId)
        implements CardEffect {

    public DawnbreakReclaimerEffect(CardPredicate filter) {
        this(filter, null, null);
    }
}
