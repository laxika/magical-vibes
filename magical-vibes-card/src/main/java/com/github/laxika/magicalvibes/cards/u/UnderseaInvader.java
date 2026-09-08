package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "KHM", collectorNumber = "78")
public class UnderseaInvader extends Card {

    public UnderseaInvader() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
