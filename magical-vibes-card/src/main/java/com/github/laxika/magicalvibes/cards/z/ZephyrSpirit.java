package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "RAV", collectorNumber = "76")
public class ZephyrSpirit extends Card {

    public ZephyrSpirit() {
        addEffect(EffectSlot.ON_BLOCK, ReturnToHandEffect.self());
    }
}
