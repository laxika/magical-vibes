package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "115")
public class ResurrectedCultist extends Card {

    public ResurrectedCultist() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterWithCounter(CounterType.FINALITY)
                                .enterWithCounterCount(1)
                                .build()
                ),
                "{2}{B}{B}: Return this card from your graveyard to the battlefield with a finality counter on it. "
                        + "Activate only if there are four or more card types among cards in your graveyard and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new Delirium(),
                "Activate only if there are four or more card types among cards in your graveyard."
        ));
    }
}
