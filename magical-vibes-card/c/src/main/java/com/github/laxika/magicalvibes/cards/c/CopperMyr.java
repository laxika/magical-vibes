package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "SOM", collectorNumber = "146")
@CardRegistration(set = "MRD", collectorNumber = "158")
@CardRegistration(set = "HOP", collectorNumber = "109")
public class CopperMyr extends Card {

    public CopperMyr() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
