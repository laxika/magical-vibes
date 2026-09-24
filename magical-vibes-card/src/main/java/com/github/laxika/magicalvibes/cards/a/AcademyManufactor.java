package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AcademyManufactorTokenReplacementEffect;

@CardRegistration(set = "MH2", collectorNumber = "219")
public class AcademyManufactor extends Card {

    public AcademyManufactor() {
        addEffect(EffectSlot.STATIC, new AcademyManufactorTokenReplacementEffect());
    }
}
