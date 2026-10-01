package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyTargetActivatedOrTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "33")
@CardRegistration(set = "WHO", collectorNumber = "638")
@CardRegistration(set = "WHO", collectorNumber = "351")
@CardRegistration(set = "WHO", collectorNumber = "942")
public class AdricMathematicalGenius extends Card {

    public AdricMathematicalGenius() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}",
                List.of(new CopyTargetActivatedOrTriggeredAbilityEffect()),
                "{2}{U}, {T}: Copy target activated or triggered ability you control. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.ACTIVATED_ABILITY,
                                        StackEntryType.TRIGGERED_ABILITY)),
                                new StackEntryControlledByPredicate())),
                        "Target must be an activated or triggered ability you control.")));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "Ultimate Sacrifice — {1}{U}, Sacrifice Adric: Counter target activated or triggered ability.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.ACTIVATED_ABILITY,
                                StackEntryType.TRIGGERED_ABILITY)),
                        "Target must be an activated or triggered ability.")));
    }
}
