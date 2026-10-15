package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "ZNR", collectorNumber = "23")
public class LegionAngel extends Card {

    public LegionAngel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchOutsideGameForCardToHandEffect(
                new CardNamedPredicate("Legion Angel")));
    }
}
