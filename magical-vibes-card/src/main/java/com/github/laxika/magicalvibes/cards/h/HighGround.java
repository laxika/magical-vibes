package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "20")
@CardRegistration(set = "EXO", collectorNumber = "7")
public class HighGround extends Card {

    public HighGround() {
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
    }
}
