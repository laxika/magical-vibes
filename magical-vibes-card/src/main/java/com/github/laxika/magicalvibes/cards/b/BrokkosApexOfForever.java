package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardCast;

@CardRegistration(set = "IKO", collectorNumber = "179")
public class BrokkosApexOfForever extends Card {

    public BrokkosApexOfForever() {
        addCastingOption(new GraveyardCast("{2}{U/B}{G}{G}"));
    }
}
