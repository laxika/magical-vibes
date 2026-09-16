package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowPlayMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "2XM", collectorNumber = "98")
public class MagusOfTheWill extends Card {

    public MagusOfTheWill() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(
                        new ExileSelfCost(),
                        new AllowPlayMatchingCardsFromGraveyardThisTurnEffect(new CardTruePredicate()),
                        new ExileOwnCardsInsteadOfGraveyardUntilEndOfTurnEffect()),
                "{2}{B}, {T}, Exile this creature: Until end of turn, you may play lands and cast spells from your graveyard. "
                        + "If a card would be put into your graveyard from anywhere this turn, exile that card instead."
        ));
    }
}
