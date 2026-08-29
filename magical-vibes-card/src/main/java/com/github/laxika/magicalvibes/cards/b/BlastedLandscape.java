package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "USG", collectorNumber = "319")
public class BlastedLandscape extends Card {

    public BlastedLandscape() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // Cycling {2} ({2}, Discard this card: Draw a card.)
        addCycling("{2}");
    }
}
