package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Lets the controller choose up to one matching card from their hand or graveyard to put onto
 * the battlefield. Choosing no card represents the optional instruction.
 */
public record PutCardFromHandOrGraveyardOntoBattlefieldEffect(
        CardPredicate predicate, String label, CounterType enterWithCounter, int enterWithCounterCount,
        boolean grantHaste, boolean returnToHandAtEndStep)
        implements CardEffect {

    public PutCardFromHandOrGraveyardOntoBattlefieldEffect {
        if (enterWithCounterCount < 0) {
            throw new IllegalArgumentException("Counter count cannot be negative");
        }
        if (enterWithCounter == null && enterWithCounterCount != 0) {
            throw new IllegalArgumentException("A counter type is required when counters are added");
        }
    }

    public PutCardFromHandOrGraveyardOntoBattlefieldEffect(CardPredicate predicate, String label) {
        this(predicate, label, null, 0, false, false);
    }

    public PutCardFromHandOrGraveyardOntoBattlefieldEffect(
            CardPredicate predicate, String label, boolean grantHaste, boolean returnToHandAtEndStep) {
        this(predicate, label, null, 0, grantHaste, returnToHandAtEndStep);
    }

    public PutCardFromHandOrGraveyardOntoBattlefieldEffect(
            CardPredicate predicate, String label, CounterType enterWithCounter) {
        this(predicate, label, enterWithCounter, 1, false, false);
    }

    public PutCardFromHandOrGraveyardOntoBattlefieldEffect(
            CardPredicate predicate, String label, CounterType enterWithCounter, int enterWithCounterCount) {
        this(predicate, label, enterWithCounter, enterWithCounterCount, false, false);
    }

    public PutCardFromHandOrGraveyardOntoBattlefieldEffect(
            CardPredicate predicate, String label, CounterType enterWithCounter,
            boolean grantHaste, boolean returnToHandAtEndStep) {
        this(predicate, label, enterWithCounter, 1, grantHaste, returnToHandAtEndStep);
    }
}
