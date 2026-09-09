package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "119")
public class UnlivingLegionnaire extends Card {

    public UnlivingLegionnaire() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}{B}",
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .upTo(true)
                                .build(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)
                ),
                "Power-up — {5}{B}{B}: Return up to one target creature card from your graveyard to your hand. "
                        + "Put two +1/+1 counters on this creature. Activate each power-up ability only once. "
                        + "Reduce the cost by its mana cost if it entered this turn."
        ).withPowerUp());
    }
}
