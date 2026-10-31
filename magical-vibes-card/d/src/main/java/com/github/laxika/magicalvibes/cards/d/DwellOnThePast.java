package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "TOR", collectorNumber = "124")
public class DwellOnThePast extends Card {

    public DwellOnThePast() {
        addSpellEffect(new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 4));
    }
}
