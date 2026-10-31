package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

@CardRegistration(set = "ME4", collectorNumber = "139")
@CardRegistration(set = "2ED", collectorNumber = "180")
@CardRegistration(set = "LEA", collectorNumber = "179")
@CardRegistration(set = "LEB", collectorNumber = "180")
public class TwoHeadedGiantOfForiys extends Card {

    public TwoHeadedGiantOfForiys() {
        addStaticEffect(new GrantAdditionalBlockEffect(1));
    }
}
