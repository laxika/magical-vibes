package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTriggeringPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "2X2", collectorNumber = "6")
public class AnointerOfValor extends Card {

    public AnointerOfValor() {
        addTrigger(Triggers.creatureAttacks(), UnlessPaysEffect.youMayPay("{3}", QueueReflexiveAbilityEffect.aboutEntryTarget(PutCountersEffect.onEach(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsTriggeringPermanentPredicate()))).withPrompt("Pay {3} to put a +1/+1 counter on the attacking creature?"));
    }
}
