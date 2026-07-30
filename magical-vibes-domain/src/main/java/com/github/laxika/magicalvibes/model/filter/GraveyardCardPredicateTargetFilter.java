package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

/**
 * Restricts a target group to cards in a graveyard — the graveyard counterpart of
 * {@link PermanentPredicateTargetFilter}. Declaring the scope per group is what lets one spell take
 * two graveyard targets with different scopes ("target instant or sorcery card from your graveyard
 * and target instant or sorcery card from an opponent's graveyard" — Spelltwine).
 *
 * @param predicate extra restriction on the card ({@code null} = any card)
 * @param scope which players' graveyards the target may be chosen from
 */
public record GraveyardCardPredicateTargetFilter(CardPredicate predicate, GraveyardSearchScope scope)
        implements TargetFilter {
}
