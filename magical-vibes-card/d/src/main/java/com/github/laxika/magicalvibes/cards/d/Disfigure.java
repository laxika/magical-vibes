package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ZEN", collectorNumber = "87")
@CardRegistration(set = "M20", collectorNumber = "95")
@CardRegistration(set = "BRO", collectorNumber = "91")
@CardRegistration(set = "A25", collectorNumber = "87")
@CardRegistration(set = "DDR", collectorNumber = "46")
@CardRegistration(set = "2X2", collectorNumber = "74")
public class Disfigure extends Card {

    public Disfigure() {
        addSpellEffect(BoostEffect.target(-2, -2));
    }
}
