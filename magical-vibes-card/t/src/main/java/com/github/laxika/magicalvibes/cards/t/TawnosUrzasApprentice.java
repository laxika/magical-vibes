package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyTargetActivatedOrTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C18", collectorNumber = "45")
public class TawnosUrzasApprentice extends Card {

    public TawnosUrzasApprentice() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{R}",
                List.of(new CopyTargetActivatedOrTriggeredAbilityEffect()),
                "{U}{R}, {T}: Copy target activated or triggered ability you control from an artifact source. You may choose new targets for the copy. (Mana abilities can't be targeted.)",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY,
                                        StackEntryType.TRIGGERED_ABILITY)),
                                new StackEntryControlledByPredicate(),
                                new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT)))),
                        "Target must be an activated or triggered ability you control from an artifact source.")));
    }
}
