package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "M12", collectorNumber = "215")
public class RustedSentinel extends Card {

    public RustedSentinel() {
        // This creature enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
