package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "M14", collectorNumber = "207")
@CardRegistration(set = "DST", collectorNumber = "112")
public class DarksteelIngot extends Card {

    public DarksteelIngot() {
        // Indestructible is auto-loaded from Scryfall.
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
