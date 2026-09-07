package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "BLB", collectorNumber = "198")
public class ThreeTreeRootweaver extends Card {

    public ThreeTreeRootweaver() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
