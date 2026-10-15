package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "160")
public class BranchblightStalker extends Card {

    public BranchblightStalker() {
        addEffect(EffectSlot.STATIC, new ToxicEffect(2));
    }
}
