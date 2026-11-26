package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ECL", collectorNumber = "5")
public class AppealToEirdu extends Card {

    public AppealToEirdu() {
        target(1, 2)
                .addSpellEffect(BoostEffect.target(2, 1));
    }
}
