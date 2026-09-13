package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "VMA", collectorNumber = "7")
public class MoxPearl extends Card {

    public MoxPearl() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
