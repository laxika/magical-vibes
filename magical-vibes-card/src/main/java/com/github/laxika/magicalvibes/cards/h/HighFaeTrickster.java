package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;

@CardRegistration(set = "FDN", collectorNumber = "40")
public class HighFaeTrickster extends Card {

    public HighFaeTrickster() {
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(null));
    }
}
