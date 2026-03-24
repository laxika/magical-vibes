package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;

@CardRegistration(set = "XLN", collectorNumber = "288")
public class SunBlessedMount extends Card {

    public SunBlessedMount() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Huatli, Dinosaur Knight"),
                "Search your library and/or graveyard for a card named Huatli, Dinosaur Knight?"
        ));
    }
}
