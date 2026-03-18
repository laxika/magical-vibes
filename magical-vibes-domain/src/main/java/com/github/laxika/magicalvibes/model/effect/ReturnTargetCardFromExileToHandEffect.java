package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns a targeted exiled card to its owner's hand. The card must match the optional filter
 * predicate. When {@code ownedOnly} is {@code true}, only cards in the controller's own exile
 * zone can be targeted (MTG "you own").
 *
 * @param filter    predicate restricting which exiled cards qualify (e.g. cards with flashback);
 *                  {@code null} means any exiled card
 * @param ownedOnly {@code true} to restrict targeting to cards the controller owns (i.e. cards
 *                  in the controller's exile zone); {@code false} to allow targeting any player's
 *                  exiled cards
 */
public record ReturnTargetCardFromExileToHandEffect(CardPredicate filter, boolean ownedOnly) implements CardEffect {

    @Override
    public boolean canTargetExile() {
        return true;
    }
}
