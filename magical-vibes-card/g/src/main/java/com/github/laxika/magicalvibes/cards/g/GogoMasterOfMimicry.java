package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyTargetAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "54")
@CardRegistration(set = "FIN", collectorNumber = "377")
@CardRegistration(set = "FIN", collectorNumber = "437")
@CardRegistration(set = "FIN", collectorNumber = "522")
public class GogoMasterOfMimicry extends Card {

    public GogoMasterOfMimicry() {
        setCantBeCopied(true);

        addActivatedAbility(new ActivatedAbility(true, "{X}{X}",
                List.of(new CopyTargetAbilityEffect()),
                "{X}{X}, {T}: Copy target activated or triggered ability you control X times. You may choose new targets for the copies.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY,
                                        StackEntryType.TRIGGERED_ABILITY)),
                                new StackEntryControlledByPredicate())),
                        "Target must be an activated or triggered ability you control."))
                .withMinimumXValue(1));
    }
}
