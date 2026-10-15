package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class MurkwaterPathway extends Card {

    public MurkwaterPathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
