package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerAndPutCounterOnSelfEffect;

@CardRegistration(set = "LTC", collectorNumber = "176")
public class SelflessSquire extends Card {

    public SelflessSquire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantStaticEffectToSourceUntilEndOfTurnEffect(
                        new PreventDamageToControllerAndPutCounterOnSelfEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, true)));
    }
}
