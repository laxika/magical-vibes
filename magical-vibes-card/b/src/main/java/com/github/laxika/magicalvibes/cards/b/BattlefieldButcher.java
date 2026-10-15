package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "86")
public class BattlefieldButcher extends Card {

    public BattlefieldButcher() {
        CardsInGraveyard creatureCardsInGraveyard = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new ReduceActivationCostEffect(creatureCardsInGraveyard),
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)
                ),
                "{5}, {T}: Each opponent loses 2 life. This ability costs {1} less to activate for each creature card in your graveyard."
        ));
    }
}
