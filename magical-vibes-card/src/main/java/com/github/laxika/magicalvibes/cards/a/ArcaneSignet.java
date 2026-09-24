package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "SLD", collectorNumber = "201")
@CardRegistration(set = "SLD", collectorNumber = "589")
@CardRegistration(set = "SLD", collectorNumber = "820")
@CardRegistration(set = "SLD", collectorNumber = "908")
@CardRegistration(set = "SLD", collectorNumber = "916")
@CardRegistration(set = "SLD", collectorNumber = "1492")
@CardRegistration(set = "SLD", collectorNumber = "1641")
@CardRegistration(set = "SLD", collectorNumber = "1919")
@CardRegistration(set = "SLD", collectorNumber = "1924")
@CardRegistration(set = "ANB", collectorNumber = "117")
@CardRegistration(set = "SOC", collectorNumber = "127")
@CardRegistration(set = "TLE", collectorNumber = "315")
@CardRegistration(set = "TMC", collectorNumber = "57")
@CardRegistration(set = "HOC", collectorNumber = "95")
@CardRegistration(set = "SLZ", collectorNumber = "94")
@CardRegistration(set = "SLZ", collectorNumber = "215")
@CardRegistration(set = "SLZ", collectorNumber = "336")
@CardRegistration(set = "ECC", collectorNumber = "55")
@CardRegistration(set = "ECC", collectorNumber = "56")
@CardRegistration(set = "CMM", collectorNumber = "367")
@CardRegistration(set = "CMM", collectorNumber = "653")
public class ArcaneSignet extends Card {

    public ArcaneSignet() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
