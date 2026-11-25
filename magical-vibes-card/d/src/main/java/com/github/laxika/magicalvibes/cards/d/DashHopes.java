package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "68")
public class DashHopes extends Card {

    public DashHopes() {
        addTrigger(Triggers.youCastThisSpell(), UnlessPaysEffect.mayPay(Subjects.eachPlayer(), new PayLifeCost(5),
                CounterSpellEffect.triggeringSpell()));
        addSpellEffect(new CounterSpellEffect());
    }
}
