package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exiles a targeted graveyard card, creates a copy of it, and offers the copy for casting. */
public record ExileTargetCardFromGraveyardAndMayCastCopyEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        int lifeLossOnCast,
        boolean targetPutIntoGraveyardFromAnywhereThisTurn,
        boolean withoutPayingManaCost,
        CardEffect afterSuccessfulCastEffect
) implements CardEffect {
    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(CardPredicate filter, GraveyardSearchScope scope, boolean withoutPayingManaCost) {
        this(filter, scope, 0, false, withoutPayingManaCost, null);
    }

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
        CardPredicate filter, GraveyardSearchScope scope) {
        this(filter, scope, 0, false, true, null);
    }

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
        CardPredicate filter, GraveyardSearchScope scope, int lifeLossOnCast) {
        this(filter, scope, lifeLossOnCast, false, true, null);
    }

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
            CardPredicate filter, GraveyardSearchScope scope, int lifeLossOnCast,
            boolean targetPutIntoGraveyardFromAnywhereThisTurn) {
        this(filter, scope, lifeLossOnCast, targetPutIntoGraveyardFromAnywhereThisTurn, true, null);
    }

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
            CardPredicate filter, GraveyardSearchScope scope, int lifeLossOnCast,
            boolean targetPutIntoGraveyardFromAnywhereThisTurn, boolean withoutPayingManaCost) {
        this(filter, scope, lifeLossOnCast, targetPutIntoGraveyardFromAnywhereThisTurn,
                withoutPayingManaCost, null);
    }

    /**
     * Exile the card and you may cast a copy of it without paying its mana cost; {@code afterSuccessfulCastEffect}
     * happens if you do (Soundwave's "If you do, transform Soundwave").
     */
    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
            CardPredicate filter, GraveyardSearchScope scope, CardEffect afterSuccessfulCastEffect) {
        this(filter, scope, 0, false, true, afterSuccessfulCastEffect);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }
}
