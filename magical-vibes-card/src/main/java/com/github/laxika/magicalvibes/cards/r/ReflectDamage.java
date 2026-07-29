package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReflectNextDamageFromChosenSourceToItsControllerEffect;

@CardRegistration(set = "MIR", collectorNumber = "277")
public class ReflectDamage extends Card {

    public ReflectDamage() {
        addEffect(EffectSlot.SPELL, new ReflectNextDamageFromChosenSourceToItsControllerEffect());
    }
}
