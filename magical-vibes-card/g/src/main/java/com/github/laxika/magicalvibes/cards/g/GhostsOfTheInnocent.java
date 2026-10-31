package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.HalveAllDamageEffect;

@CardRegistration(set = "RAV", collectorNumber = "20")
public class GhostsOfTheInnocent extends Card {

    public GhostsOfTheInnocent() {
        addStaticEffect(new HalveAllDamageEffect());
    }
}
