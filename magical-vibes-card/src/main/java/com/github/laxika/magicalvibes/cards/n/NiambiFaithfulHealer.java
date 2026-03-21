package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;

@CardRegistration(set = "DOM", collectorNumber = "272")
public class NiambiFaithfulHealer extends Card {

    public NiambiFaithfulHealer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Teferi, Timebender"),
                "Search your library and/or graveyard for a card named Teferi, Timebender?"
        ));
    }
}
