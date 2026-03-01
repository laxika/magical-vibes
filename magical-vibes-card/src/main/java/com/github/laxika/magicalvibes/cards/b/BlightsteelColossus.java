package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "99")
public class BlightsteelColossus extends Card {

    public BlightsteelColossus() {
        setShufflesIntoLibraryFromGraveyard(true);
    }
}
