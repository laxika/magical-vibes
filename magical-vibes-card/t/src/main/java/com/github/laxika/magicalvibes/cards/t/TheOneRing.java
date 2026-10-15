package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromEverythingUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "44")
@CardRegistration(set = "HOC", collectorNumber = "84")
public class TheOneRing extends Card {

    public TheOneRing() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new WasCast(), new GrantProtectionFromEverythingUntilNextTurnEffect()));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new LoseLifeEffect(new CountersOnSource(CounterType.BURDEN), LoseLifeRecipient.CONTROLLER));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PutCountersOnSelfEffect(CounterType.BURDEN),
                        new DrawCardEffect(new CountersOnSource(CounterType.BURDEN))
                ),
                "{T}: Put a burden counter on The One Ring, then draw a card for each burden counter on The One Ring."
        ));
    }
}
