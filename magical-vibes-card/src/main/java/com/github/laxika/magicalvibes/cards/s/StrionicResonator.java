package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyTargetTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "224")
public class StrionicResonator extends Card {

    public StrionicResonator() {
        // {2}, {T}: Copy target triggered ability you control. You may choose new targets for the copy.
        addActivatedAbility(new ActivatedAbility(true, "{2}",
                List.of(new CopyTargetTriggeredAbilityEffect()),
                "{2}, {T}: Copy target triggered ability you control. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.TRIGGERED_ABILITY)),
                                new StackEntryControlledByPredicate())),
                        "Target must be a triggered ability you control.")));
    }
}
