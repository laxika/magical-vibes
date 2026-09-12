package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "VMA", collectorNumber = "8")
public class MoxRuby extends Card {

    public MoxRuby() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
