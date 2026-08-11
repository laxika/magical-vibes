package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "THS", collectorNumber = "180")
public class SylvanCaryatid extends Card {

    public SylvanCaryatid() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
