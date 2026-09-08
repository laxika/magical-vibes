package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "AER", collectorNumber = "82")
public class FrontlineRebel extends Card {

    public FrontlineRebel() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
