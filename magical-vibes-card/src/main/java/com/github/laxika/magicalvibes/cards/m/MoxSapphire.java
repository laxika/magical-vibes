package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "VMA", collectorNumber = "9")
@CardRegistration(set = "2ED", collectorNumber = "266")
public class MoxSapphire extends Card {

    public MoxSapphire() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
