package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

@CardRegistration(set = "2ED", collectorNumber = "180")
public class TwoHeadedGiantOfForiys extends Card {

    public TwoHeadedGiantOfForiys() {
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
    }
}
