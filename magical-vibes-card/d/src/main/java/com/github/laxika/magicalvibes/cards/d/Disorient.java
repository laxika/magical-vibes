package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "M10", collectorNumber = "48")
public class Disorient extends Card {

    public Disorient() {
        addSpellEffect(new BoostTargetCreatureEffect(-7, 0));
    }
}
