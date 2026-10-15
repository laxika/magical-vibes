package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToSelfAndAddPlusOneCounterEffect;

@CardRegistration(set = "THB", collectorNumber = "296")
public class IronscaleHydra extends Card {

    public IronscaleHydra() {
        addEffect(EffectSlot.STATIC, new PreventCombatDamageToSelfAndAddPlusOneCounterEffect());
    }
}
