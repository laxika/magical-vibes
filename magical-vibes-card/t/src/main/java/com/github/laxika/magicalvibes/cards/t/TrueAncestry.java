package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "TLA", collectorNumber = "199")
public class TrueAncestry extends Card {

    public TrueAncestry() {
        addSpellEffect(new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardIsPermanentPredicate(), 1));
        addSpellEffect(CreateTokenEffect.ofClueToken(1).withInvestigate(false));
    }
}
