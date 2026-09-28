package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "OTC", collectorNumber = "34")
@CardRegistration(set = "OTC", collectorNumber = "70")
public class TowerWinder extends Card {

    public TowerWinder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryAndOrGraveyardForCardToHandEffect(
                        new CardNamedPredicate("Command Tower")));
    }
}
