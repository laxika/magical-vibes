package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandAndCardToGraveyardEffect;

@CardRegistration(set = "DOM", collectorNumber = "93")
public class FinalParting extends Card {

    public FinalParting() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForCardToHandAndCardToGraveyardEffect());
    }
}
