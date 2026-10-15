package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "16")
public class TayamLuminousEnigma extends Card {

    public TayamLuminousEnigma() {
        // Each other creature you control enters with an additional vigilance counter on it.
        addEffect(EffectSlot.STATIC, new ControlledPermanentsEnterWithAdditionalCountersEffect(
                new PermanentIsCreaturePredicate(), CounterType.VIGILANCE, 1));

        // {3}, Remove three counters from among creatures you control: Mill three cards, then
        // return a permanent card with mana value 3 or less from your graveyard to the battlefield.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new RemoveCounterFromControlledPermanentCost(
                                3, new PermanentIsCreaturePredicate(), false),
                        new MillEffect(3, MillRecipient.CONTROLLER),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAllOfPredicate(List.of(
                                        new CardIsPermanentPredicate(),
                                        new CardMaxManaValuePredicate(3))))
                                .mandatory(true)
                                .build()
                ),
                "{3}, Remove three counters from among creatures you control: Mill three cards, "
                        + "then return a permanent card with mana value 3 or less from your graveyard "
                        + "to the battlefield."
        ));
    }
}
