package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "133")
public class OpenTheGates extends Card {

    public OpenTheGates() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                CardPredicateUtils.basicLand(),
                new CardSubtypePredicate(CardSubtype.GATE)))));
    }
}
