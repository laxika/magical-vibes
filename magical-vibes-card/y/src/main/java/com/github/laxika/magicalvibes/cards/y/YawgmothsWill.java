package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowPlayMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "USG", collectorNumber = "171")
@CardRegistration(set = "VMA", collectorNumber = "148")
public class YawgmothsWill extends Card {

    public YawgmothsWill() {
        addSpellEffect(new AllowPlayMatchingCardsFromGraveyardThisTurnEffect(new CardTruePredicate()));
        addSpellEffect(new ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect());
    }
}
