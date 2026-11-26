package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "RTR", collectorNumber = "144")
@CardRegistration(set = "MM3", collectorNumber = "151")
@CardRegistration(set = "GK2", collectorNumber = "60")
@CardRegistration(set = "2X2", collectorNumber = "178")
public class AugerSpree extends Card {

    public AugerSpree() {
        // Target creature gets +4/-4 until end of turn.
        addSpellEffect(BoostEffect.target(4, -4));
    }
}
