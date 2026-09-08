package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromSubtypeEffect;

@CardRegistration(set = "KHM", collectorNumber = "125")
public class CalamityBearer extends Card {

    public CalamityBearer() {
        addEffect(EffectSlot.STATIC, new DoubleDamageFromSubtypeEffect(CardSubtype.GIANT));
    }
}
