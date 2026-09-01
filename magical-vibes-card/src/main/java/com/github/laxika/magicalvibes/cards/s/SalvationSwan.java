package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "28")
public class SalvationSwan extends Card {

    public SalvationSwan() {
        PermanentAllOfPredicate nonFlyingCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))
        ));

        target(new PermanentPredicateTargetFilter(
                nonFlyingCreatureYouControl,
                "Target must be a creature you control without flying"
        ), 0, 1).addEffect(
                EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.BIRD),
                        FlickerEffect.exileTargetReturnAtEndStepWithCounter(CounterType.FLYING)));
    }
}
