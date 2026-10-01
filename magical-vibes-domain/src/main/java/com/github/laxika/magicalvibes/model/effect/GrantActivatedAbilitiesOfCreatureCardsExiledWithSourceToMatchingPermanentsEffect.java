package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Grants matching permanents controlled by the source's controller the activated abilities of creature cards exiled with the source. */
public record GrantActivatedAbilitiesOfCreatureCardsExiledWithSourceToMatchingPermanentsEffect(
        PermanentPredicate targetFilter) implements CardEffect {
}
