package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "AER", collectorNumber = "82")
@CardRegistration(set = "KLR", collectorNumber = "125")
public class FrontlineRebel extends Card {

    public FrontlineRebel() {
        addStaticEffect(new MustAttackEffect());
    }
}
