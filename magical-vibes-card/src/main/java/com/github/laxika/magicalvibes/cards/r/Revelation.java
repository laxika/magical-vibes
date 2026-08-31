package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayWithHandsRevealedEffect;

@CardRegistration(set = "CHR", collectorNumber = "68")
@CardRegistration(set = "LEG", collectorNumber = "202")
public class Revelation extends Card {

    public Revelation() {
        addEffect(EffectSlot.STATIC, new PlayWithHandsRevealedEffect());
    }
}
