package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

@CardRegistration(set = "GRN", collectorNumber = "148")
public class VividRevival extends Card {

    public VividRevival() {
        addSpellEffect(new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardIsMulticoloredPredicate(), 3));
        addSpellEffect(new ExileSpellEffect());
    }
}
