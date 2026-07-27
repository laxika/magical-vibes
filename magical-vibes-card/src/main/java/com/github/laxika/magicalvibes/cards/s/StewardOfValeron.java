package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;


@CardRegistration(set = "ALA", collectorNumber = "198")
public class StewardOfValeron extends Card {

    public StewardOfValeron() {
        // {T}: Add {G}. (Vigilance is auto-loaded from Scryfall.)
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
