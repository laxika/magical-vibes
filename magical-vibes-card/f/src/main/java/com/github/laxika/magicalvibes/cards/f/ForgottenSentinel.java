package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "M21", collectorNumber = "231")
public class ForgottenSentinel extends Card {

    public ForgottenSentinel() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
