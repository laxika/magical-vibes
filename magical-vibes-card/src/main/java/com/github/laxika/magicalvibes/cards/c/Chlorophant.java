package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.CounterType;

@CardRegistration(set = "ODY", collectorNumber = "234")
public class Chlorophant extends Card {

    public Chlorophant() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                "Put a +1/+1 counter on Chlorophant?"));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(new GraveyardCardThreshold(7, null),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.UPKEEP_TRIGGERED,
                        new MayEffect(new PutCountersOnSourceEffect(1, 1, 1),
                                "Put another +1/+1 counter on Chlorophant?"),
                        GrantScope.SELF)));
    }
}
