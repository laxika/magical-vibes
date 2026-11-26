package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "M10", collectorNumber = "48")
public class Disorient extends Card {

    public Disorient() {
        addSpellEffect(BoostEffect.target(-7, 0));
    }
}
