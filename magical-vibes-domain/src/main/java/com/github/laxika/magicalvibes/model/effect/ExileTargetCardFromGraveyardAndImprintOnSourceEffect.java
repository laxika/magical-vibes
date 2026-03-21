package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles the targeted card from a graveyard and imprints it on the source permanent
 * (tracked in permanentExiledCards). The card must match the given predicate (if non-null).
 * Used by cards like Myr Welder ({@code CardTypePredicate(ARTIFACT)}),
 * Rona, Disciple of Gix ({@code CardIsHistoricPredicate()}).
 *
 * @param filter predicate restricting which graveyard cards are valid targets;
 *               {@code null} means any card
 */
public record ExileTargetCardFromGraveyardAndImprintOnSourceEffect(CardPredicate filter) implements CardEffect {
    @Override public boolean canTargetGraveyard() { return true; }
}
