package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "MRD", collectorNumber = "282")
public class GreatFurnace extends Card {

    public GreatFurnace() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
