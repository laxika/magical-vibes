package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast [filtered] spells from your graveyard this turn" (Liliana, Untouched by Death's
 * −3). Unlike {@link AllowCastTargetCardFromGraveyardThisTurnEffect}, which permits one chosen
 * card, this grants a turn-scoped blanket permission for every matching card — including cards
 * that reach the graveyard later in the turn. Unlike a
 * {@link CastSpellsFromGraveyardPermission}, which is a static ability of a permanent on the
 * battlefield, this is a one-shot grant recorded on {@code GameData} and dropped at turn cleanup.
 */
public record AllowCastMatchingCardsFromGraveyardThisTurnEffect(
        CardPredicate filter,
        boolean singleUse,
        CounterType entryCounterType,
        CardSubtype grantedSubtype,
        ForageOrPayManaCost additionalCost,
        CounterType enterWithCounter,
        int enterWithCounterCount) implements CardEffect {

    public AllowCastMatchingCardsFromGraveyardThisTurnEffect(CardPredicate filter) {
        this(filter, false, null, null, null, null, 0);
    }

    public AllowCastMatchingCardsFromGraveyardThisTurnEffect(
            CardPredicate filter, ForageOrPayManaCost additionalCost,
            CounterType enterWithCounter, int enterWithCounterCount) {
        this(filter, false, null, null, additionalCost, enterWithCounter, enterWithCounterCount);
    }

    public static AllowCastMatchingCardsFromGraveyardThisTurnEffect oneShotWithEntryEffects(
            CardPredicate filter, CounterType entryCounterType, CardSubtype grantedSubtype) {
        return new AllowCastMatchingCardsFromGraveyardThisTurnEffect(
                filter, true, entryCounterType, grantedSubtype, null, null, 0);
    }
}
