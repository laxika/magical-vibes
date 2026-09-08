package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "USG", collectorNumber = "171")
public class YawgmothsWill extends Card {

    public YawgmothsWill() {
        addEffect(EffectSlot.SPELL, new AllowPlayMatchingCardsFromGraveyardThisTurnEffect(new CardTruePredicate()));
        addEffect(EffectSlot.SPELL, new ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect());
    }
}
