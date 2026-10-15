package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "TMC", collectorNumber = "13")
@CardRegistration(set = "TMC", collectorNumber = "86")
public class KrangTheAllPowerful extends Card {

    public KrangTheAllPowerful() {
        addEffect(EffectSlot.STATIC, new AdditionalTriggeredAbilityEffect(
                new PermanentTruePredicate(), null, true, false));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new NthCardDrawTriggerEffect(2,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
