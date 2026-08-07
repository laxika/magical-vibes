package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "Return a [predicate] card from your graveyard to your hand" as the payable side of a
 * {@link ForcedCostOrElseEffect} ("sacrifice this creature unless you return a basic land card
 * from your graveyard to your hand" — Harvest Wurm).
 *
 * <p>No matching card in the graveyard makes the cost unpayable, so the fallback effects resolve
 * without a prompt. Accepting the "you may" prompt opens a graveyard choice whose chosen card goes
 * to its owner's hand.
 *
 * @param predicate filter for which graveyard cards can be returned to pay the cost
 */
public record ReturnCardFromGraveyardToHandCost(CardPredicate predicate) implements CostEffect {
}
