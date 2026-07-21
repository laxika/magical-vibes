package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards that have an embalm or eternalize ability — a graveyard-activated ability that
 * creates a token copy of its source (see {@code ActivatedAbility.isEmbalmOrEternalize()}, the
 * engine's structural marker for both keywords). Used by Vizier of the Anointed's search.
 */
public record CardHasEmbalmOrEternalizePredicate() implements CardPredicate {
}
