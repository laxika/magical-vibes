package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "SOM", collectorNumber = "168")
@CardRegistration(set = "MRD", collectorNumber = "187")
@CardRegistration(set = "HOP", collectorNumber = "115")
public class IronMyr extends Card {

    public IronMyr() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
