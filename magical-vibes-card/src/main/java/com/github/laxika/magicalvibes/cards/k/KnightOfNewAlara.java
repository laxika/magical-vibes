package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostOtherMulticoloredCreaturesByColorCountEffect;

@CardRegistration(set = "ARB", collectorNumber = "70")
public class KnightOfNewAlara extends Card {

    public KnightOfNewAlara() {
        // Each other multicolored creature you control gets +1/+1 for each of its colors.
        addEffect(EffectSlot.STATIC, new BoostOtherMulticoloredCreaturesByColorCountEffect(1, 1));
    }
}
