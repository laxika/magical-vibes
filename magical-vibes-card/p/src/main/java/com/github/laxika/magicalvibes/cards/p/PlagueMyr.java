package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "MBS", collectorNumber = "125")
@CardRegistration(set = "TD2", collectorNumber = "51")
@CardRegistration(set = "ONC", collectorNumber = "139")
public class PlagueMyr extends Card {

    public PlagueMyr() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
