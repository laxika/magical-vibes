package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesInsteadOfDyingThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "RNA", collectorNumber = "70")
public class CryOfTheCarnarium extends Card {

    public CryOfTheCarnarium() {
        addSpellEffect(new BoostAllCreaturesEffect(-2, -2));
        addSpellEffect(ExileGraveyardCardsEffect.allPlayersMatchingFromBattlefieldThisTurn(
                new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(new ExileCreaturesInsteadOfDyingThisTurnEffect());
    }
}
