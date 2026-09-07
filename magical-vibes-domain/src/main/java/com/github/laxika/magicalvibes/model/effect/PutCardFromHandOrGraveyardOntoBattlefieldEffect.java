package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the controller choose up to one matching card from their hand or graveyard to put onto
 * the battlefield. Choosing no card represents the optional instruction.
 */
public record PutCardFromHandOrGraveyardOntoBattlefieldEffect(
        CardPredicate predicate, String label, CounterType enterWithCounter,
        boolean grantHaste, boolean returnToHandAtEndStep)
        implements CardEffect {

    public PutCardFromHandOrGraveyardOntoBattlefieldEffect(CardPredicate predicate, String label) {
        this(predicate, label, null, false, false);
    }

    public PutCardFromHandOrGraveyardOntoBattlefieldEffect(
            CardPredicate predicate, String label, boolean grantHaste, boolean returnToHandAtEndStep) {
        this(predicate, label, null, grantHaste, returnToHandAtEndStep);
    }

    public PutCardFromHandOrGraveyardOntoBattlefieldEffect(
            CardPredicate predicate, String label, CounterType enterWithCounter) {
        this(predicate, label, enterWithCounter, false, false);
    }
}
