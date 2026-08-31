package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "103")
public class HeartlessAct extends Card {

    private static final PermanentPredicate CREATURE_WITHOUT_COUNTERS = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.ANY))));

    public HeartlessAct() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target creature with no counters on it",
                        new DestroyTargetPermanentEffect(CREATURE_WITHOUT_COUNTERS),
                        new PermanentPredicateTargetFilter(
                                CREATURE_WITHOUT_COUNTERS,
                                "Target must be a creature with no counters on it.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove up to three counters from target creature",
                        new RemoveUpToCountersFromTargetEffect(3, new PermanentIsCreaturePredicate()),
                        TargetFilters.creature())
        )));
    }
}
