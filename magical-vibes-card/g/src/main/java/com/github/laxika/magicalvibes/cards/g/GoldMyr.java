package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "SOM", collectorNumber = "157")
@CardRegistration(set = "MRD", collectorNumber = "180")
@CardRegistration(set = "TD2", collectorNumber = "6")
@CardRegistration(set = "HOP", collectorNumber = "114")
@CardRegistration(set = "NEC", collectorNumber = "153")
public class GoldMyr extends Card {

    public GoldMyr() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
