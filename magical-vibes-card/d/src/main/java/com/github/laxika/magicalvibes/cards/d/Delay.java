package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileWithSuspendCountersEffect;

@CardRegistration(set = "FUT", collectorNumber = "35")
@CardRegistration(set = "SLD", collectorNumber = "1788")
@CardRegistration(set = "TSR", collectorNumber = "61")
public class Delay extends Card {

    public Delay() {
        addEffect(EffectSlot.SPELL, new CounterSpellAndExileWithSuspendCountersEffect(3));
    }
}
