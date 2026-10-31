package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;

@CardRegistration(set = "BRO", collectorNumber = "196")
public class WastefulHarvest extends Card {

    public WastefulHarvest() {
        addSpellEffect(new MillControllerAndMayReturnMilledPermanentToHandEffect(5));
    }
}
