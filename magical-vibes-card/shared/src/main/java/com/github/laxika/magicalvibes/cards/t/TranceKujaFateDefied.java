package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

public class TranceKujaFateDefied extends Card {

    public TranceKujaFateDefied() {
        addStaticEffect(DoubleDamageFromCreaturesEffect.fromSourcesYouControl(
                new PermanentHasSubtypePredicate(CardSubtype.WIZARD), new CardSubtypePredicate(CardSubtype.WIZARD)));
    }
}
