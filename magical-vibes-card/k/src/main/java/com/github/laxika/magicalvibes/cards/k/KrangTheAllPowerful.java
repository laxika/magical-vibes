package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMC", collectorNumber = "13")
@CardRegistration(set = "TMC", collectorNumber = "86")
public class KrangTheAllPowerful extends Card {

    public KrangTheAllPowerful() {
        addStaticEffect(AdditionalTriggeredAbilityEffect.forDrawCausedTriggers());
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.opponentDrawsSecondCardEachTurn(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
