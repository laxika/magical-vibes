package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToSelfEffect;

@CardRegistration(set = "RIX", collectorNumber = "6")
public class EverdawnChampion extends Card {

    public EverdawnChampion() {
        // "Prevent all combat damage that would be dealt to this creature."
        addEffect(EffectSlot.STATIC, new PreventAllCombatDamageToSelfEffect());
    }
}
