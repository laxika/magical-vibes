package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "TDC", collectorNumber = "101")
public class ShadowSummoning extends Card {

    public ShadowSummoning() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSpirit(2).withTapped(true));
    }
}
