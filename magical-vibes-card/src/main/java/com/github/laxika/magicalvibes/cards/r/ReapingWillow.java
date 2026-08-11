package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "240")
public class ReapingWillow extends Card {

    public ReapingWillow() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(2)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W/B}",
                List.of(
                        new RemoveCounterFromSourceCost(2),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardMaxManaValuePredicate(3))))
                                .targetGraveyard(true)
                                .build()
                ),
                "{1}{W/B}, Remove two counters from this creature: Return target creature card with "
                        + "mana value 3 or less from your graveyard to the battlefield. Activate only "
                        + "as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
