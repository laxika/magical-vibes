package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;

@CardRegistration(set = "RNA", collectorNumber = "135")
public class RampagingRendhorn extends Card {

    public RampagingRendhorn() {
        addEffect(EffectSlot.STATIC, new RiotEffect());
    }
}
