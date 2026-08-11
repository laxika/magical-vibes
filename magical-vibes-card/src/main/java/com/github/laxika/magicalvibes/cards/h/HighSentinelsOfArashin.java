package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "13")
public class HighSentinelsOfArashin extends Card {

    public HighSentinelsOfArashin() {
        PermanentCount otherCounteredCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE))),
                CountScope.CONTROLLER,
                true);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(otherCounteredCreatures, otherCounteredCreatures));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "{3}{W}: Put a +1/+1 counter on target creature.",
                TargetFilters.creature()));
    }
}
