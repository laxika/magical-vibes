package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

public class BirthrightBoon extends Card {

    public BirthrightBoon() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.AURA),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)
        ))));
    }
}
