package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;

@CardRegistration(set = "RNA", collectorNumber = "215")
public class ZhurTaaGoblin extends Card {

    public ZhurTaaGoblin() {
        addStaticEffect(new RiotEffect());
    }
}
