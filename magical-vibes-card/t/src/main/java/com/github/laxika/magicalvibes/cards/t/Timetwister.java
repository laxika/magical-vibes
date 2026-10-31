package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;

@CardRegistration(set = "VMA", collectorNumber = "3")
@CardRegistration(set = "2ED", collectorNumber = "85")
@CardRegistration(set = "LEA", collectorNumber = "84")
@CardRegistration(set = "LEB", collectorNumber = "85")
public class Timetwister extends Card {

    public Timetwister() {
        // Each player shuffles their hand and graveyard into their library, then draws seven cards.
        addSpellEffect(new EachPlayerShufflesZonesIntoLibraryEffect());
        addSpellEffect(new EachPlayerDrawsCardEffect(7));
    }
}
