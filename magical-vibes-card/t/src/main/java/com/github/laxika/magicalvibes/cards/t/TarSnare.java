package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "OGW", collectorNumber = "90")
public class TarSnare extends Card {

    public TarSnare() {
        addSpellEffect(BoostEffect.target(-3, -2));
    }
}
