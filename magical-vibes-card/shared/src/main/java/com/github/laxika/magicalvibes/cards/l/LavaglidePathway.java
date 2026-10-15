package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

public class LavaglidePathway extends Card {

    public LavaglidePathway() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
