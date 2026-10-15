package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;

@CardRegistration(set = "RNA", collectorNumber = "215")
public class ZhurTaaGoblin extends Card {

    public ZhurTaaGoblin() {
        addEffect(EffectSlot.STATIC, new RiotEffect());
    }
}
