package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentEffect;

@CardRegistration(set = "MMQ", collectorNumber = "154")
public class QuagmireLamprey extends Card {

    public QuagmireLamprey() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new PutCounterOnCombatOpponentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1),
                TriggerMode.PER_BLOCKER);
    }
}
