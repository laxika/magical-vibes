package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndBySelfEffect;

@CardRegistration(set = "M13", collectorNumber = "52")
public class FogBank extends Card {

    public FogBank() {
        // "Prevent all combat damage that would be dealt to and dealt by this creature."
        addEffect(EffectSlot.STATIC, new PreventAllCombatDamageToAndBySelfEffect());
    }
}
