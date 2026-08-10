package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "MRD", collectorNumber = "285")
public class TreeOfTales extends Card {

    public TreeOfTales() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
