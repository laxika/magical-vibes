package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;


@CardRegistration(set = "HOU", collectorNumber = "164")
@CardRegistration(set = "M12", collectorNumber = "212")
@CardRegistration(set = "M19", collectorNumber = "239")
public class Manalith extends Card {

    public Manalith() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
