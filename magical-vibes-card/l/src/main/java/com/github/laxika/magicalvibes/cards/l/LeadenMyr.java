package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "SOM", collectorNumber = "170")
@CardRegistration(set = "MRD", collectorNumber = "191")
@CardRegistration(set = "HOP", collectorNumber = "116")
public class LeadenMyr extends Card {

    public LeadenMyr() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
