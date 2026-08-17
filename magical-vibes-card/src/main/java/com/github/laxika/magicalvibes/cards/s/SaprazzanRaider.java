package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "MMQ", collectorNumber = "102")
public class SaprazzanRaider extends Card {

    public SaprazzanRaider() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, ReturnToHandEffect.self());
    }
}
