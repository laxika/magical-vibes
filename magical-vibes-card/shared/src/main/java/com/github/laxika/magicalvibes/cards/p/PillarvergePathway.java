package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class PillarvergePathway extends Card {

    public PillarvergePathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
