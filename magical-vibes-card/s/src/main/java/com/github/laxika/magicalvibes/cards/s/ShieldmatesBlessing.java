package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "ZEN", collectorNumber = "35")
public class ShieldmatesBlessing extends Card {

    public ShieldmatesBlessing() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTarget(3));
    }
}
