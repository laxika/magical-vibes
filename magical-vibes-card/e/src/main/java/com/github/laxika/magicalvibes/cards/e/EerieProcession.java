package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "58")
public class EerieProcession extends Card {

    public EerieProcession() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.ARCANE)));
    }
}
