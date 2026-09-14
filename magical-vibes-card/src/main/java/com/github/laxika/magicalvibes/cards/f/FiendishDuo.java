package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToOpponentsEffect;

@CardRegistration(set = "GN2", collectorNumber = "4")
public class FiendishDuo extends Card {

    public FiendishDuo() {
        addEffect(EffectSlot.STATIC, new DoubleDamageToOpponentsEffect());
    }
}
