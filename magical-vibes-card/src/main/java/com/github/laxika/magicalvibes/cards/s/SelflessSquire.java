package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerAndPutCounterOnSelfEffect;

@CardRegistration(set = "C21", collectorNumber = "103")
@CardRegistration(set = "LTC", collectorNumber = "176")
@CardRegistration(set = "MKC", collectorNumber = "82")
@CardRegistration(set = "C16", collectorNumber = "4")
public class SelflessSquire extends Card {

    public SelflessSquire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantStaticEffectToSourceUntilEndOfTurnEffect(
                        new PreventDamageToControllerAndPutCounterOnSelfEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, true)));
    }
}
