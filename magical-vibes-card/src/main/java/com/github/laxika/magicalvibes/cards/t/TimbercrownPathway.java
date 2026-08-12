package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class TimbercrownPathway extends Card {

    public TimbercrownPathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
