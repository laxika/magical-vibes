package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;

@CardRegistration(set = "FUT", collectorNumber = "83")
@CardRegistration(set = "MMA", collectorNumber = "79")
@CardRegistration(set = "TSR", collectorNumber = "110")
public class DeepcavernImp extends Card {

    public DeepcavernImp() {
        addStaticEffect(new EchoEffect(new DiscardCardTypeCost(null, null)));
    }
}
