package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "DTK", collectorNumber = "100")
public class Flatten extends Card {

    public Flatten() {
        addSpellEffect(BoostEffect.target(-4, -4));
    }
}
