package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EyeForAnEyeEffect;

@CardRegistration(set = "5ED", collectorNumber = "30")
@CardRegistration(set = "4ED", collectorNumber = "25")
public class EyeForAnEye extends Card {

    public EyeForAnEye() {
        addEffect(EffectSlot.SPELL, new EyeForAnEyeEffect());
    }
}
