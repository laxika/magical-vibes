package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToSelfEffect;

@CardRegistration(set = "ROE", collectorNumber = "70")
public class GuardGomazoa extends Card {

    public GuardGomazoa() {
        // "Prevent all combat damage that would be dealt to this creature."
        addEffect(EffectSlot.STATIC, new PreventAllCombatDamageToSelfEffect());
    }
}
