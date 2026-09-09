package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "25")
public class PossessedGoat extends Card {

    public PossessedGoat() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                        new GrantColorEffect(CardColor.BLACK, GrantScope.SELF),
                        new GrantSubtypeEffect(CardSubtype.DEMON, GrantScope.SELF)
                ),
                "{3}, Discard a card: Put three +1/+1 counters on this creature and it becomes a "
                        + "black Demon in addition to its other colors and types. Activate only once."
        ).withMaxActivationsPerGame(1));
    }
}
