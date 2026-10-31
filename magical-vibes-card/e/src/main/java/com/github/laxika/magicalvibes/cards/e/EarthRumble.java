package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandThenFightEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "174")
public class EarthRumble extends Card {

    public EarthRumble() {
        target(TargetFilters.landYouControl())
                .addSpellEffect(new EarthbendTargetLandThenFightEffect(2));
    }
}
