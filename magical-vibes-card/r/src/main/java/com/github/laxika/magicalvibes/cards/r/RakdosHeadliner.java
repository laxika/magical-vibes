package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "MH2", collectorNumber = "210")
public class RakdosHeadliner extends Card {

    public RakdosHeadliner() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect(new DiscardCardTypeCost(null, null)));
    }
}
