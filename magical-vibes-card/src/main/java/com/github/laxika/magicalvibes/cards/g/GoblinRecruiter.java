package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSubtypeCardsToTopEffect;

@CardRegistration(set = "6ED", collectorNumber = "186")
public class GoblinRecruiter extends Card {

    public GoblinRecruiter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryForSubtypeCardsToTopEffect(CardSubtype.GOBLIN));
    }
}
