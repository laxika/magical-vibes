package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "ECL", collectorNumber = "178")
public class GreatForestDruid extends Card {

    public GreatForestDruid() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
