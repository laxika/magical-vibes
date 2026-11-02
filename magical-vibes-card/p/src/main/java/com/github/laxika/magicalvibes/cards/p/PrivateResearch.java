package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "41")
public class PrivateResearch extends Card {

    public PrivateResearch() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                        new PutCountersOnSelfEffect(CounterType.PAGE),
                        "Put a page counter on this Aura?"))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new DrawCardEffect(new CountersOnSource(CounterType.PAGE)));
    }
}
