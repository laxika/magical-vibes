package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "UDS", collectorNumber = "61")
public class FesteringWound extends Card {

    public FesteringWound() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new MayEffect(PutCountersEffect.onSelf(CounterType.INFECTION),
                                "Put an infection counter on Festering Wound?"))
                .addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                        DealDamageEffect.to(Subjects.triggeringPlayer(), new CountersOnSource(CounterType.INFECTION)));
    }
}
