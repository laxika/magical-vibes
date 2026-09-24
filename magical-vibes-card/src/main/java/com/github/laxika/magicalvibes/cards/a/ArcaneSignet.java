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
public class ArcaneSignet extends Card {

    public ArcaneSignet() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
