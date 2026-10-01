package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/** Puts time counters on a qualifying card in exile and gives it suspend. */
public record PutTimeCountersOnTargetExiledCardWithSuspendEffect(int amount) implements CardEffect {

    private static final CardPredicate NONLAND = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    public PutTimeCountersOnTargetExiledCardWithSuspendEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exiledCards(NONLAND));
    }
}
