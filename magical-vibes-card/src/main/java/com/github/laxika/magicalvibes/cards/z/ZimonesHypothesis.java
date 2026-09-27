package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseManaValueParityAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCreaturesOfChosenPowerParityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOC", collectorNumber = "206")
public class ZimonesHypothesis extends Card {

    public ZimonesHypothesis() {
        addEffect(EffectSlot.SPELL, new MayEffect(
                new PutCounterOnChosenPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentIsCreaturePredicate()),
                "Put a +1/+1 counter on a creature?"));
        addEffect(EffectSlot.SPELL, new ChooseManaValueParityAtResolutionEffect());
        addEffect(EffectSlot.SPELL, new ReturnAllCreaturesOfChosenPowerParityEffect());
    }
}
