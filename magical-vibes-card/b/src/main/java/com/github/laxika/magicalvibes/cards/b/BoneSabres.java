package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "88")
public class BoneSabres extends Card {

    public BoneSabres() {
        // Whenever equipped creature attacks, put four +1/+1 counters on it.
        addTrigger(Triggers.attacks(),
                new PutCountersEffect(Subjects.triggering(),
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
