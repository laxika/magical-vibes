package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantControllerShroudEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "23")
@CardRegistration(set = "8ED", collectorNumber = "27")
public class IvoryMask extends Card {

    public IvoryMask() {
        addEffect(EffectSlot.STATIC, new GrantControllerShroudEffect());
    }
}
