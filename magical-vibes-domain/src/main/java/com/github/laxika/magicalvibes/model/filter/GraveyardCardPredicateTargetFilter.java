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
 * @param minimumPoisonCounters when non-null, cards in an opponent's graveyard are eligible only
 *                              if that opponent had at least this many poison counters as the
 *                              spell was cast; the condition is not rechecked on resolution
 */
public record GraveyardCardPredicateTargetFilter(CardPredicate predicate, GraveyardSearchScope scope,
                                                 Integer minimumPoisonCounters)
        implements TargetFilter {

    public GraveyardCardPredicateTargetFilter(CardPredicate predicate, GraveyardSearchScope scope) {
        this(predicate, scope, null);
    }

    public GraveyardCardPredicateTargetFilter {
        if (minimumPoisonCounters != null && minimumPoisonCounters < 0) {
            throw new IllegalArgumentException("minimumPoisonCounters cannot be negative");
        }
    }
}
