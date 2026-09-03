package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ForageOrPayManaCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "103")
public class OsteomancerAdept extends Card {

    public OsteomancerAdept() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AllowCastMatchingCardsFromGraveyardThisTurnEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        ForageOrPayManaCost.forageOnly(),
                        CounterType.FINALITY,
                        1)),
                "{T}: Until end of turn, you may cast creature spells from your graveyard by foraging in addition "
                        + "to paying their other costs. If you cast a spell this way, that creature enters with a "
                        + "finality counter on it."
        ));
    }
}
