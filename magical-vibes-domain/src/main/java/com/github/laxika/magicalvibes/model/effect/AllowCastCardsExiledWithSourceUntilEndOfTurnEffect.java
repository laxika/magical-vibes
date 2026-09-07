package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * Grants the controller one temporary permission to cast a matching card exiled with the source
 * permanent until end of turn.
 *
 * <p>The permission is source-linked, but the activated ability that creates it remains independent
 * of the source permanent. Casting one card consumes the grant; activating the ability again creates
 * another grant.</p>
 */
public record AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(
        CardPredicate filter,
        boolean withoutPayingManaCost,
        boolean targetSpecificCard,
        boolean putOnBottomOfOwnersLibrary,
        boolean ownOnly
) implements CardEffect {

    public AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(CardPredicate filter,
                                                               boolean withoutPayingManaCost) {
        this(filter, withoutPayingManaCost, false, false, false);
    }

    public AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(CardPredicate filter,
                                                               boolean withoutPayingManaCost,
                                                               boolean ownOnly) {
        this(filter, withoutPayingManaCost, false, false, ownOnly);
    }

    public static AllowCastCardsExiledWithSourceUntilEndOfTurnEffect targeted(
            CardPredicate filter, boolean withoutPayingManaCost, boolean putOnBottomOfOwnersLibrary) {
        return new AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(
                filter, withoutPayingManaCost, true, putOnBottomOfOwnersLibrary, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetSpecificCard
                ? TargetSpec.benign(TargetPredicates.exiledCards(
                        filter == null ? new CardTruePredicate() : filter))
                : TargetSpec.NONE;
    }
}
