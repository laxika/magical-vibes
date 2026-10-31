package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandAndCardToGraveyardEffect;

@CardRegistration(set = "DOM", collectorNumber = "93")
@CardRegistration(set = "CMM", collectorNumber = "160")
public class FinalParting extends Card {

    public FinalParting() {
        addSpellEffect(new SearchLibraryForCardToHandAndCardToGraveyardEffect());
    }
}
