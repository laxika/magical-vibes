package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "SNC", collectorNumber = "100")
public class Antagonize extends Card {

    public Antagonize() {
        addSpellEffect(BoostEffect.target(4, 3));
    }
}
