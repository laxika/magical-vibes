package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "202")
public class JhoiraAgelessInnovator extends Card {

    public JhoiraAgelessInnovator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PutCountersOnSelfEffect(CounterType.INGENUITY, 2),
                        new PutCardToBattlefieldEffect(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardManaValueAtMostSourceCountersPredicate(CounterType.INGENUITY))), "artifact")
                ),
                "{T}: Put two ingenuity counters on Jhoira, then you may put an artifact card with mana value X "
                        + "or less from your hand onto the battlefield, where X is the number of ingenuity counters "
                        + "on Jhoira."
        ));
    }
}
