package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "59")
public class SabotageStrategist extends Card {

    public SabotageStrategist() {
        addEffect(EffectSlot.ON_CREATURES_ATTACK_YOU,
                new BoostAllOwnCreaturesEffect(-1, 0).forDeclaredAttackers());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)),
                "Exhaust — {5}{U}{U}: Put three +1/+1 counters on this creature. "
                        + "(Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
