package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "THS", collectorNumber = "218")
public class OpalineUnicorn extends Card {

    public OpalineUnicorn() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
