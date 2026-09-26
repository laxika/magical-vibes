package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMostCreaturesOrTiedEffect;

@CardRegistration(set = "C13", collectorNumber = "80")
public class HoodedHorror extends Card {

    public HoodedHorror() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedIfDefenderControlsMostCreaturesOrTiedEffect());
    }
}
