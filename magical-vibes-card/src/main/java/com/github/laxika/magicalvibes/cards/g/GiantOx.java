package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CrewWithToughnessEffect;

@CardRegistration(set = "KHM", collectorNumber = "11")
public class GiantOx extends Card {

    public GiantOx() {
        addEffect(EffectSlot.STATIC, new CrewWithToughnessEffect());
    }
}
