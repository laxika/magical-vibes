package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSubtypeCardsToTopEffect;

@CardRegistration(set = "ODY", collectorNumber = "186")
public class DwarvenRecruiter extends Card {

    public DwarvenRecruiter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryForSubtypeCardsToTopEffect(CardSubtype.DWARF));
    }
}
