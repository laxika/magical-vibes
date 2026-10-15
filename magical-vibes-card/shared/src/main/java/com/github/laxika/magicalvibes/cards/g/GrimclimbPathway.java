package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class GrimclimbPathway extends Card {

    public GrimclimbPathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
