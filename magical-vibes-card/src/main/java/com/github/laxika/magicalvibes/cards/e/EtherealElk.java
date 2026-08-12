package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;

@CardRegistration(set = "M20", collectorNumber = "299")
public class EtherealElk extends Card {

    public EtherealElk() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Vivien, Nature's Avenger"),
                "Search your library and/or graveyard for a card named Vivien, Nature's Avenger?"
        ));
    }
}
