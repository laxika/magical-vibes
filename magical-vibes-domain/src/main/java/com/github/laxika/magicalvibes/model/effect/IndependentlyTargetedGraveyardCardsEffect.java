package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/** Describes independently optional, single-card target groups in one graveyard. */
public interface IndependentlyTargetedGraveyardCardsEffect extends CardEffect {

    /** The filters for the target groups, in the order in which they are announced. */
    List<CardPredicate> targetFilters();

    /** Human-readable target descriptions corresponding to {@link #targetFilters()}. */
    List<String> targetDescriptions();
}
