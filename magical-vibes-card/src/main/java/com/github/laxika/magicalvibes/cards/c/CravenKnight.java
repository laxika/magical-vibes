package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "POR", collectorNumber = "85")
public class CravenKnight extends Card {

    public CravenKnight() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
    }
}
