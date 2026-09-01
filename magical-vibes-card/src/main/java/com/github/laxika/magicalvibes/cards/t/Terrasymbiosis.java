package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "EOE", collectorNumber = "210")
public class Terrasymbiosis extends Card {

    public Terrasymbiosis() {
        addEffect(EffectSlot.ON_CONTROLLER_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE,
                new OncePerTurnTriggerEffect(
                        new MayEffect(new DrawCardEffect(new EventValue()), "Draw that many cards?")));
    }
}
