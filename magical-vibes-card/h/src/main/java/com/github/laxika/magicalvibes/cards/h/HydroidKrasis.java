package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "183")
@CardRegistration(set = "SLD", collectorNumber = "1332")
@CardRegistration(set = "2X2", collectorNumber = "228")
@CardRegistration(set = "SOC", collectorNumber = "315")
@CardRegistration(set = "M3C", collectorNumber = "266")
@CardRegistration(set = "MKC", collectorNumber = "212")
public class HydroidKrasis extends Card {

    public HydroidKrasis() {
        addTrigger(Triggers.youCastThisSpell(),
                new GainLifeEffect(new Divided(new XValue(), 2)),
                new DrawCardEffect(new Divided(new XValue(), 2)));
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
