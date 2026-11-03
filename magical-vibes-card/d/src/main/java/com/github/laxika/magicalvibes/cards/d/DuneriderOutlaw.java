package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SelfDealtDamageToOpponentThisTurn;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "86")
@CardRegistration(set = "TSR", collectorNumber = "112")
public class DuneriderOutlaw extends Card {

    public DuneriderOutlaw() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.GREEN)));
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new SelfDealtDamageToOpponentThisTurn()),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
