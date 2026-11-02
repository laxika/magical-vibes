package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "182")
@CardRegistration(set = "MM2", collectorNumber = "161")
@CardRegistration(set = "DDP", collectorNumber = "22")
@CardRegistration(set = "C18", collectorNumber = "161")
@CardRegistration(set = "E01", collectorNumber = "73")
public class ScuteMob extends Card {

    public ScuteMob() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ConditionalEffect(new ControlsPermanentCount(5, new PermanentIsLandPredicate()),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 4)));
    }
}
