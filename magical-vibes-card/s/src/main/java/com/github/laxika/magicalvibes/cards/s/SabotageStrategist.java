package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "59")
public class SabotageStrategist extends Card {

    public SabotageStrategist() {
        addTrigger(Triggers.creaturesAttackYou(),
                BoostEffect.each(Subjects.triggeringAttackers(), -1, 0));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                List.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 3)),
                "Exhaust — {5}{U}{U}: Put three +1/+1 counters on this creature. "
                        + "(Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
