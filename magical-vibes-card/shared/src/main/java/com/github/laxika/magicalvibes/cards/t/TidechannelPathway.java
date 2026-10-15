package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class TidechannelPathway extends Card {

    public TidechannelPathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
