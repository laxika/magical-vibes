package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "66")
public class SigurdJarlOfRavensthorpe extends Card {

    public SigurdJarlOfRavensthorpe() {
        var sagaYouControl = new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.SAGA),
                "Target must be a Saga you control");

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a lore counter on target Saga you control",
                                new PutCounterOnTargetPermanentEffect(CounterType.LORE),
                                sagaYouControl),
                        new ChooseOneEffect.ChooseOneOption(
                                "Remove a lore counter from target Saga you control",
                                new RemoveCountersFromTargetPermanentEffect(
                                        CounterType.LORE, 1, new PermanentHasSubtypePredicate(CardSubtype.SAGA)),
                                sagaYouControl)
                ))),
                "Boast — {1}: Put a lore counter on target Saga you control or remove one from it. "
                        + "Activate only if this creature attacked this turn and only once each turn.",
                sagaYouControl,
                null,
                1,
                null
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast().withModalChoiceAtActivation());

        addEffect(EffectSlot.ON_YOU_PUT_LORE_COUNTERS_ON_SAGA,
                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
