package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "MH2", collectorNumber = "237")
public class SteelDromedary extends Card {

    public SteelDromedary() {
        addStaticEffect(new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
        addStaticEffect(new DoesntUntapWithCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new MayEffect(
                                new MoveCounterFromSourceToTargetCreatureEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE),
                                "Move a +1/+1 counter from Steel Dromedary onto the target creature?"));
    }
}
