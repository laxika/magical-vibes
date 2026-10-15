package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ZEN", collectorNumber = "74")
public class TrapmakersSnare extends Card {

    public TrapmakersSnare() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.TRAP)));
    }
}
