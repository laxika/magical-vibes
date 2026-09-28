package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyTargetActivatedOrTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardIdPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySourceHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntrySourceIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "19")
@CardRegistration(set = "DMC", collectorNumber = "95")
public class ThePeregrineDynamo extends Card {

    public ThePeregrineDynamo() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new CopyTargetActivatedOrTriggeredAbilityEffect()),
                "{1}, {T}: Copy target activated or triggered ability you control from another legendary source that's not a commander. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY,
                                        StackEntryType.TRIGGERED_ABILITY)),
                                new StackEntryControlledByPredicate(),
                                new StackEntrySourceHasSupertypePredicate(CardSupertype.LEGENDARY),
                                new StackEntryNotPredicate(new StackEntrySourceIsCommanderPredicate()),
                                new StackEntryNotPredicate(new StackEntryCardIdPredicate(getId())))),
                        "Target must be an activated or triggered ability you control from another legendary source that's not a commander.")));
    }
}
