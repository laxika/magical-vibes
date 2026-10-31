package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "FIN", collectorNumber = "188")
public class GoobbueGardener extends Card {

    public GoobbueGardener() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
