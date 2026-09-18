package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "M14", collectorNumber = "207")
@CardRegistration(set = "DST", collectorNumber = "112")
@CardRegistration(set = "PIO", collectorNumber = "254")
@CardRegistration(set = "C13", collectorNumber = "241")
@CardRegistration(set = "CMD", collectorNumber = "245")
public class DarksteelIngot extends Card {

    public DarksteelIngot() {
        // Indestructible is auto-loaded from Scryfall.
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
