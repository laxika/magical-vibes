package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayPayLifeToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "PLC", collectorNumber = "68")
public class DashHopes extends Card {

    public DashHopes() {
        addEffect(EffectSlot.ON_SELF_CAST, new AnyPlayerMayPayLifeToCounterSpellEffect(5));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
