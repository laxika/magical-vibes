package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "242")
@CardRegistration(set = "LCI", collectorNumber = "310")
public class UchbenbakTheGreatMistake extends Card {

    public UchbenbakTheGreatMistake() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}{B}",
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterWithCounter(CounterType.FINALITY)
                                .enterWithCounterCount(1)
                                .build()
                ),
                "{4}{U}{B}: Return this card from your graveyard to the battlefield with a finality counter on it. "
                        + "Activate only if there are eight or more permanent cards in your graveyard and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new GraveyardCardThreshold(8, new CardIsPermanentPredicate()),
                "Activate only if there are eight or more permanent cards in your graveyard."
        ));
    }
}
