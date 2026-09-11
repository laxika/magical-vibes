package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "OGW", collectorNumber = "16")
public class CallTheGatewatch extends Card {

    public CallTheGatewatch() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardTypePredicate(CardType.PLANESWALKER)));
    }
}
