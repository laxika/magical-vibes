package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "94")
@CardRegistration(set = "SPM", collectorNumber = "264")
@CardRegistration(set = "OM1", collectorNumber = "92")
public class SpinneretAndSpiderling extends Card {

    public SpinneretAndSpiderling() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withInterveningIf(new MinimumAttackingCreaturesOfSubtype(2, CardSubtype.SPIDER)),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.dealsDamage(),
                TriggerOptions.DEFAULT.withInterveningIf(new EventValueAtLeast(4)),
                new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
