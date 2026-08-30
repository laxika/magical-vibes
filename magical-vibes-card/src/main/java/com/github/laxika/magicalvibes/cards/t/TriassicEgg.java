package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "110")
@CardRegistration(set = "LEG", collectorNumber = "297")
public class TriassicEgg extends Card {

    public TriassicEgg() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new PutCountersOnSelfEffect(CounterType.HATCHLING)),
                "{3}, {T}: Put a hatchling counter on this artifact."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                                "Put a creature card from your hand onto the battlefield?"
                        )
                ),
                "Sacrifice this artifact: You may put a creature card from your hand onto the battlefield."
        ).withRequiredSourceCounters(CounterType.HATCHLING, 2));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .build()
                ),
                "Sacrifice this artifact: Return target creature card from your graveyard to the battlefield."
        ).withRequiredSourceCounters(CounterType.HATCHLING, 2));
    }
}
