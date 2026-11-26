package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;

@CardRegistration(set = "PLS", collectorNumber = "81")
@CardRegistration(set = "DMU", collectorNumber = "164")
public class GaeasMight extends Card {

    public GaeasMight() {
        addSpellEffect(BoostEffect.target(
                new BasicLandTypesAmongControlledLands(), new BasicLandTypesAmongControlledLands()));
    }
}
