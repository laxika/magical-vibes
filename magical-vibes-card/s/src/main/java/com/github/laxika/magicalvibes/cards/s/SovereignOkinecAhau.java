package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnControlledCreaturesByPowerAboveBasePowerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "240")
@CardRegistration(set = "LCI", collectorNumber = "309")
public class SovereignOkinecAhau extends Card {

    public SovereignOkinecAhau() {
        // Ward {2}.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));

        addTrigger(Triggers.attacks(),
                new PutCountersOnControlledCreaturesByPowerAboveBasePowerEffect(
                        CounterType.PLUS_ONE_PLUS_ONE));
    }
}
