package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "21")
@CardRegistration(set = "MSC", collectorNumber = "315")
public class MethodsOfTheMighty extends Card {

    public MethodsOfTheMighty() {
        setAllowSharedTargets(true);

        PermanentPredicate tappedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate()
        ));

        // Choose one or more — Destroy target artifact. Destroy target tapped creature. Put a
        // +1/+1 counter on each creature you control.
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        TargetFilters.artifact()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target tapped creature",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(tappedCreature,
                                "Target must be a tapped creature")),
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on each creature you control",
                        new PutCounterOnEachControlledPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()))
        )));
    }
}
