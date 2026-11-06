package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "23")
public class ReveredUnicorn extends Card {

    public ReveredUnicorn() {
        // Cumulative upkeep {1}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}"));
        // When this creature leaves the battlefield, you gain life equal to the number of age
        // counters on it: the leaving permanent's, as it last existed on the battlefield.
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new GainLifeEffect(new CountersOnTriggeringPermanent(CounterType.AGE)));
    }
}
