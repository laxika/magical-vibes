package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "TDC", collectorNumber = "101")
public class ShadowSummoning extends Card {

    public ShadowSummoning() {
        addSpellEffect(CreateTokenEffect.whiteSpirit(2).withTapped(true));
    }
}
