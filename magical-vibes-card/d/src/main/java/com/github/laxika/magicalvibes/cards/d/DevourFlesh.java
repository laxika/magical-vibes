package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect;

@CardRegistration(set = "GTC", collectorNumber = "63")
@CardRegistration(set = "PIO", collectorNumber = "90")
public class DevourFlesh extends Card {

    public DevourFlesh() {
        addSpellEffect(new SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(false, true));
    }
}
