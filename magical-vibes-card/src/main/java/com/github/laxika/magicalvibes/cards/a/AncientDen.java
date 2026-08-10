package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "MRD", collectorNumber = "278")
public class AncientDen extends Card {

    public AncientDen() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
