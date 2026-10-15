package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "128")
public class FungalBehemoth extends Card {

    public FungalBehemoth() {
        PermanentCounterSum plusOneCountersOnCreaturesYouControl = new PermanentCounterSum(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentIsCreaturePredicate(),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                plusOneCountersOnCreaturesYouControl,
                plusOneCountersOnCreaturesYouControl));
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE,
                new MayEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on target creature?"));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{G}{G}",
                List.of(),
                "Suspend X\u2014{X}{G}{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHandX());
    }
}
