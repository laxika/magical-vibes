package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/** Exchanges the rules text characteristics of the triggering creature and another creature. */
public record ExchangeTextBoxesEffect(PermanentPredicate targetPredicate)
        implements CardEffect, TriggeringPermanentSourceEffect {

    public ExchangeTextBoxesEffect() {
        this(anotherCreaturePredicate());
    }

    public static PermanentPredicate anotherCreaturePredicate() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), targetPredicate);
    }
}
