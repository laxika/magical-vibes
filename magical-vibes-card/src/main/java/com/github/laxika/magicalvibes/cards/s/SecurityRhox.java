package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "220")
public class SecurityRhox extends Card {

    public SecurityRhox() {
        addCastingOption(new AlternateHandCast(List.of(ManaCastingCost.treasureOnly("{R}{G}"))));
    }
}
