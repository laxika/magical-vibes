package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyTargetActivatedOrTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "225")
public class ScientistSupremeOfAIM extends Card {

    public ScientistSupremeOfAIM() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(2), new CopyTargetActivatedOrTriggeredAbilityEffect()),
                "Pay 2 life: Copy target activated or triggered ability you control from an artifact source. "
                        + "You may choose new targets for the copy. Activate only during your turn and only once each turn.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY,
                                        StackEntryType.TRIGGERED_ABILITY)),
                                new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT)),
                                new StackEntryControlledByPredicate())),
                        "Target must be an activated or triggered ability you control from an artifact source."),
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN));
    }
}
