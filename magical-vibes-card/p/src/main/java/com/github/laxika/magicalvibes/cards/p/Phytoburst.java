package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "DGM", collectorNumber = "46")
public class Phytoburst extends Card {

    public Phytoburst() {
        addSpellEffect(BoostEffect.target(5, 5));
    }
}
