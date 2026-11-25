package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveXCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "306")
public class MercadianLift extends Card {

    public MercadianLift() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(PutCountersEffect.onSelf(CounterType.WINCH)),
                "{1}, {T}: Put a winch counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveXCountersFromSourceCost(CounterType.WINCH),
                        new PutCardToBattlefieldEffect(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardManaValueEqualsXPredicate())), "creature")
                ),
                "{T}, Remove X winch counters from this artifact: You may put a creature card with mana value X "
                        + "from your hand onto the battlefield."
        ));
    }
}
