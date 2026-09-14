package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "LEG", collectorNumber = "254")
@CardRegistration(set = "ME3", collectorNumber = "171")
public class RivenTurnbull extends Card {

    public RivenTurnbull() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
