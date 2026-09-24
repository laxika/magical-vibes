package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "ANB", collectorNumber = "117")
@CardRegistration(set = "HOC", collectorNumber = "95")
@CardRegistration(set = "SLZ", collectorNumber = "94")
@CardRegistration(set = "SLZ", collectorNumber = "215")
@CardRegistration(set = "SLZ", collectorNumber = "336")
@CardRegistration(set = "CMM", collectorNumber = "367")
@CardRegistration(set = "CMM", collectorNumber = "653")
public class ArcaneSignet extends Card {

    public ArcaneSignet() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
