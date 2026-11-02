package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "139")
public class KuldothaCackler extends Card {

    public KuldothaCackler() {
        // Whenever this creature attacks, it gets +X/+0 until end of turn, where X is the number of permanents you control with oil counters on them.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(
                new PermanentCount(new PermanentHasCountersPredicate(CounterType.OIL), CountScope.CONTROLLER),
                new Fixed(0)));
    }
}
