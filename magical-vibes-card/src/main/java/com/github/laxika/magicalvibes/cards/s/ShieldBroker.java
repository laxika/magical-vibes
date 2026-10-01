package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetWhileHasCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "29")
@CardRegistration(set = "NCC", collectorNumber = "130")
@CardRegistration(set = "BLC", collectorNumber = "174")
public class ShieldBroker extends Card {

    public ShieldBroker() {
        var targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsCommanderPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        target(new PermanentPredicateTargetFilter(
                targetFilter, "Target must be a noncommander creature you don't control"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.SHIELD))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GainControlOfTargetWhileHasCounterEffect(CounterType.SHIELD));
    }
}
