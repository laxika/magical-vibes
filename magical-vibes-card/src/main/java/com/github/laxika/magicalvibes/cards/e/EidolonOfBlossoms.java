package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "JOU", collectorNumber = "122")
public class EidolonOfBlossoms extends Card {

    public EidolonOfBlossoms() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new DrawCardEffect(1));
    }
}
