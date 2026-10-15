package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ScrycastCast;

@CardRegistration(set = "MB1", collectorNumber = "17")
public class BitingRemark extends Card {

    public BitingRemark() {
        addCastingOption(new ScrycastCast("{0}"));
    }
}
