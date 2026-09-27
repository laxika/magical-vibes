package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "C21", collectorNumber = "103")
public class SelflessSquire extends Card {

    public SelflessSquire() {
        // Flash is auto-loaded from Scryfall.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, PreventDamageEffect.allToController());
        addEffect(EffectSlot.ON_CONTROLLER_DAMAGE_PREVENTED,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
