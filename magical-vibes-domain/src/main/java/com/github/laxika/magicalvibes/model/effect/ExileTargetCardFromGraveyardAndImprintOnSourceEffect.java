package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles the targeted card from a graveyard and imprints it on the source permanent
 * (tracked in permanentExiledCards). The card must match the given predicate (if non-null).
 * Used by Myr Welder ("from a graveyard", {@code CardTypePredicate(ARTIFACT)}) and
 * Rona, Disciple of Gix ("from your graveyard", {@code CardIsHistoricPredicate()}) — hence the
 * per-card {@code scope}.
 *
 * @param filter predicate restricting which graveyard cards are valid targets;
 *               {@code null} means any card
 * @param scope  which graveyards the target may be drawn from
 */
public record ExileTargetCardFromGraveyardAndImprintOnSourceEffect(
        CardPredicate filter, GraveyardSearchScope scope) implements CardEffect {

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetPredicates.graveyardCard(scope)); }
}
