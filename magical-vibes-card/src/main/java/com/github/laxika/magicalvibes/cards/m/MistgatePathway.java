package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class MistgatePathway extends Card {

    public MistgatePathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
