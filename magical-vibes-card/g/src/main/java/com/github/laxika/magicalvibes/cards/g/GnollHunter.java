package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AttackingCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "185")
@CardRegistration(set = "HBG", collectorNumber = "212")
public class GnollHunter extends Card {

    public GnollHunter() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AttackingCreaturesTotalPowerAtLeast(6)),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
