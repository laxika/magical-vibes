package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "JOU", collectorNumber = "111")
public class SigiledSkink extends Card {

    public SigiledSkink() {
        addEffect(EffectSlot.ON_ATTACK, new ScryEffect(1));
    }
}
