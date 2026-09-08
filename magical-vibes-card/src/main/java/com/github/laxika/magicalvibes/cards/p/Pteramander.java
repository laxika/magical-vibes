package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AdaptEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "47")
public class Pteramander extends Card {

    public Pteramander() {
        CardsInGraveyard instantSorceryCardsInGraveyard = new CardsInGraveyard(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                CountScope.CONTROLLER);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}{U}",
                List.of(
                        new ReduceActivationCostEffect(instantSorceryCardsInGraveyard),
                        new AdaptEffect(4)),
                "{7}{U}: Adapt 4. This ability costs {1} less to activate for each instant and sorcery card in your graveyard."
        ));
    }
}
