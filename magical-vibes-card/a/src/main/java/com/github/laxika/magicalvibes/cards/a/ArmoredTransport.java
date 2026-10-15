package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToSelfFromBlockersEffect;

@CardRegistration(set = "GTC", collectorNumber = "226")
public class ArmoredTransport extends Card {

    public ArmoredTransport() {
        // "Prevent all combat damage that would be dealt to this creature by creatures blocking it."
        addEffect(EffectSlot.STATIC, new PreventAllCombatDamageToSelfFromBlockersEffect());
    }
}
