package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class BoulderloftPathway extends Card {

    public BoulderloftPathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
