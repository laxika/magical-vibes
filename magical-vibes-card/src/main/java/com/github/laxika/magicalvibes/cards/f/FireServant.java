package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerSpellDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "137")
public class FireServant extends Card {

    public FireServant() {
        addEffect(EffectSlot.STATIC, new DoubleControllerSpellDamageEffect(CardColor.RED));
    }
}
