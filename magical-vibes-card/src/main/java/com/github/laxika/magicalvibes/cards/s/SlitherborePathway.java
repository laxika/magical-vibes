package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class SlitherborePathway extends Card {

    public SlitherborePathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
