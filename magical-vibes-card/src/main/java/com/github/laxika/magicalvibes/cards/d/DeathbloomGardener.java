package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "DMU", collectorNumber = "159")
public class DeathbloomGardener extends Card {

    public DeathbloomGardener() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
