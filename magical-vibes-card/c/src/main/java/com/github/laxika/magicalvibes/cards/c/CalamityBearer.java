package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromSubtypeEffect;

@CardRegistration(set = "KHM", collectorNumber = "125")
@CardRegistration(set = "KHM", collectorNumber = "356")
public class CalamityBearer extends Card {

    public CalamityBearer() {
        addStaticEffect(new DoubleDamageFromSubtypeEffect(CardSubtype.GIANT));
    }
}
