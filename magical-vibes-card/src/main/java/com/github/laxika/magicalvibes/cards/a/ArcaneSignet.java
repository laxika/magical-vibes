package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "ANB", collectorNumber = "117")
@CardRegistration(set = "SOC", collectorNumber = "127")
public class ArcaneSignet extends Card {

    public ArcaneSignet() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
