package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "BFZ", collectorNumber = "177")
public class LifespringDruid extends Card {

    public LifespringDruid() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
