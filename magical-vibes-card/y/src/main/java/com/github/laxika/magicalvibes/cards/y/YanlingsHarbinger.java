package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;

@CardRegistration(set = "M20", collectorNumber = "289")
public class YanlingsHarbinger extends Card {

    public YanlingsHarbinger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Mu Yanling, Celestial Wind"),
                "Search your library and/or graveyard for a card named Mu Yanling, Celestial Wind?"
        ));
    }
}
