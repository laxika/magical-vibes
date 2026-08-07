package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;


@CardRegistration(set = "LRW", collectorNumber = "227")
@CardRegistration(set = "ORI", collectorNumber = "184")
public class LeafGilder extends Card {

    public LeafGilder() {
        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
