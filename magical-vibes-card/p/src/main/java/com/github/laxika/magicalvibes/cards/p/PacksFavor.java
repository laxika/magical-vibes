package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "GRN", collectorNumber = "139")
public class PacksFavor extends Card {

    public PacksFavor() {
        addSpellEffect(BoostEffect.target(3, 3));
    }
}
