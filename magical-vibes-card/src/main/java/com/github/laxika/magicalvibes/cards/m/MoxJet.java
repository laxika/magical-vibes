package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "VMA", collectorNumber = "6")
@CardRegistration(set = "2ED", collectorNumber = "263")
public class MoxJet extends Card {

    public MoxJet() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
