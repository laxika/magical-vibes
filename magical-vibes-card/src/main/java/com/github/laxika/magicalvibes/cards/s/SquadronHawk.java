package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsByNameToHandEffect;

@CardRegistration(set = "M11", collectorNumber = "33")
public class SquadronHawk extends Card {

    public SquadronHawk() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryForCardsByNameToHandEffect("Squadron Hawk", 3),
                "Search your library for up to three cards named Squadron Hawk?"
        ));
    }
}
