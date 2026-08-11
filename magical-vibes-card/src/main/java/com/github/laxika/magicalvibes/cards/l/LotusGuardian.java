package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "INV", collectorNumber = "305")
public class LotusGuardian extends Card {

    public LotusGuardian() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
