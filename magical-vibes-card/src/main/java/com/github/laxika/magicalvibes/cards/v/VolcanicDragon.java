package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MIR", collectorNumber = "201")
@CardRegistration(set = "POR", collectorNumber = "153")
@CardRegistration(set = "6ED", collectorNumber = "214")
@CardRegistration(set = "M12", collectorNumber = "158")
@CardRegistration(set = "M19", collectorNumber = "167")
@CardRegistration(set = "M20", collectorNumber = "336")
public class VolcanicDragon extends Card {

    public VolcanicDragon() {
        // Flying and Haste are auto-loaded from Scryfall. No other engine logic.
    }
}
