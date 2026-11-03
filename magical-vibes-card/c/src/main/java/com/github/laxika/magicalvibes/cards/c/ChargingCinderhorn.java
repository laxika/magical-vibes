package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.NoCreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C16", collectorNumber = "16")
public class ChargingCinderhorn extends Card {

    public ChargingCinderhorn() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new NoCreaturesAttackedThisTurn()),
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.FURY),
                        new DealDamageToEndStepPlayerEffect(new CountersOnSource(CounterType.FURY))));
    }
}
