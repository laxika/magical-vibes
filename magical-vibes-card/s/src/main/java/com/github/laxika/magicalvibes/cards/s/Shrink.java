package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "5ED", collectorNumber = "326")
@CardRegistration(set = "HML", collectorNumber = "97a")
@CardRegistration(set = "HML", collectorNumber = "97b")
@CardRegistration(set = "ME2", collectorNumber = "175")
public class Shrink extends Card {

    public Shrink() {
        addSpellEffect(BoostEffect.target(-5, 0));
    }
}
