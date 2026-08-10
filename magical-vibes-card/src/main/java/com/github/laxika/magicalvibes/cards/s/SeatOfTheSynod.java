package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "MRD", collectorNumber = "283")
public class SeatOfTheSynod extends Card {

    public SeatOfTheSynod() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
