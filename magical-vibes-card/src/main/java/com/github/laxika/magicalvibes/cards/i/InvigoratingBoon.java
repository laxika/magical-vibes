package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "267")
public class InvigoratingBoon extends Card {

    private static final String PROMPT = "Put a +1/+1 counter on target creature?";

    public InvigoratingBoon() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ANY_PLAYER_CYCLES, new MayEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1), PROMPT));
    }
}
