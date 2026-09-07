package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.condition.SacrificedCardMatches;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "81")
public class BoneyardDesecrator extends Card {

    public BoneyardDesecrator() {
        CardAnyOfPredicate outlaw = new CardAnyOfPredicate(List.of(
                new CardSubtypePredicate(CardSubtype.ASSASSIN),
                new CardSubtypePredicate(CardSubtype.MERCENARY),
                new CardSubtypePredicate(CardSubtype.PIRATE),
                new CardSubtypePredicate(CardSubtype.ROGUE),
                new CardSubtypePredicate(CardSubtype.WARLOCK)
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new PutCountersOnSourceEffect(1, 1, 1),
                        new ConditionalEffect(
                                new SacrificedCardMatches(outlaw, "an outlaw"),
                                CreateTokenEffect.ofTreasureToken(1)
                        )
                ),
                "{1}{B}, Sacrifice another creature: Put a +1/+1 counter on this creature. If an outlaw was sacrificed this way, create a Treasure token."
        ));
    }
}
