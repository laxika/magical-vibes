package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "51")
@CardRegistration(set = "ECL", collectorNumber = "305")
@CardRegistration(set = "ECL", collectorNumber = "383")
@CardRegistration(set = "ECL", collectorNumber = "393")
public class GlenElendraGuardian extends Card {

    public GlenElendraGuardian() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        new RemoveCounterFromSourceCost(),
                        new TargetSpellControllerDrawsCardEffect(),
                        new CounterSpellEffect()
                ),
                "{1}{U}, Remove a counter from this creature: Counter target noncreature spell. Its controller draws a card.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryNotPredicate(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))
                        ),
                        "Target must be a noncreature spell."
                )
        ));
    }
}
