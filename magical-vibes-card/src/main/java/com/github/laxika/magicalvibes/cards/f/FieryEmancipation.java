package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerDamageMultiplierEffect;

@CardRegistration(set = "M21", collectorNumber = "143")
public class FieryEmancipation extends Card {

    public FieryEmancipation() {
        addEffect(EffectSlot.STATIC, new ControllerDamageMultiplierEffect(3, null, true));
    }
}
