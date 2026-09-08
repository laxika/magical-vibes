package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "PLS", collectorNumber = "106")
public class EladamrisCall extends Card {

    public EladamrisCall() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardTypePredicate(CardType.CREATURE)));
    }
}
