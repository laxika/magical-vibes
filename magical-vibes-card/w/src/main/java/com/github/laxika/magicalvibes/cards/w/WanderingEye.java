package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayWithHandsRevealedEffect;

@CardRegistration(set = "NEM", collectorNumber = "50")
public class WanderingEye extends Card {

    public WanderingEye() {
        addEffect(EffectSlot.STATIC, new PlayWithHandsRevealedEffect());
    }
}
