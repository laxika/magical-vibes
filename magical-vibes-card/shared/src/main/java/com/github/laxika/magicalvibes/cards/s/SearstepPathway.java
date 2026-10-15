package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class SearstepPathway extends Card {

    public SearstepPathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
