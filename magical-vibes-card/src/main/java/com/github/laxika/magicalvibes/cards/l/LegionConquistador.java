package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsByNameToHandEffect;

@CardRegistration(set = "XLN", collectorNumber = "20")
public class LegionConquistador extends Card {

    public LegionConquistador() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryForCardsByNameToHandEffect("Legion Conquistador", Integer.MAX_VALUE),
                "Search your library for any number of cards named Legion Conquistador?"
        ));
    }
}
