package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "SOM", collectorNumber = "65")
@CardRegistration(set = "M21", collectorNumber = "102")
@CardRegistration(set = "OGW", collectorNumber = "85")
@CardRegistration(set = "GN2", collectorNumber = "30")
public class GraspOfDarkness extends Card {

    public GraspOfDarkness() {
        addSpellEffect(BoostEffect.target(-4, -4));
    }
}
