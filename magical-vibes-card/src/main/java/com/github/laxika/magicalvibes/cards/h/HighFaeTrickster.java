package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;

@CardRegistration(set = "FDN", collectorNumber = "40")
@CardRegistration(set = "FDN", collectorNumber = "307")
@CardRegistration(set = "FDN", collectorNumber = "375")
@CardRegistration(set = "FDN", collectorNumber = "453")
public class HighFaeTrickster extends Card {

    public HighFaeTrickster() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(null));
    }
}
