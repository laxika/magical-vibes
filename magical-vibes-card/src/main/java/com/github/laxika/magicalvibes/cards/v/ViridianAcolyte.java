package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "DST", collectorNumber = "89")
public class ViridianAcolyte extends Card {

    public ViridianAcolyte() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
