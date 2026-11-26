package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "M20", collectorNumber = "145")
@CardRegistration(set = "THB", collectorNumber = "141")
@CardRegistration(set = "STA", collectorNumber = "41")
public class Infuriate extends Card {

    public Infuriate() {
        addSpellEffect(BoostEffect.target(3, 2));
    }
}
