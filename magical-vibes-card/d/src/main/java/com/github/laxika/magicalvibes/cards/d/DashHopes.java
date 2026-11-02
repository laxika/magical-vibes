package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayPayLifeToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "68")
public class DashHopes extends Card {

    public DashHopes() {
        addTrigger(Triggers.youCastThisSpell(), new AnyPlayerMayPayLifeToCounterSpellEffect(5));
        addSpellEffect(new CounterSpellEffect());
    }
}
