package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the controller choose up to one matching card from their hand or graveyard to put onto
 * the battlefield. Choosing no card represents the optional instruction.
 */
public record PutCardFromHandOrGraveyardOntoBattlefieldEffect(CardPredicate predicate, String label)
        implements CardEffect {
}
