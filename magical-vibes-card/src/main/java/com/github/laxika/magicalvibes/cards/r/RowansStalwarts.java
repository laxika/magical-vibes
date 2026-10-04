package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;

@CardRegistration(set = "ELD", collectorNumber = "307")
public class RowansStalwarts extends Card {

    public RowansStalwarts() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Rowan, Fearless Sparkmage"),
                "Search your library and/or graveyard for a card named Rowan, Fearless Sparkmage?"
        ));
    }
}
