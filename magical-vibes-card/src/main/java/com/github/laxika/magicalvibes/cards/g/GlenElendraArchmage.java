package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EVE", collectorNumber = "22")
public class GlenElendraArchmage extends Card {

    public GlenElendraArchmage() {
        // Flying and Persist are keywords loaded from Scryfall (persist handled by
        // PermanentRemovalService); only the activated ability needs engine logic.
        // {U}, Sacrifice this creature: Counter target noncreature spell.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "{U}, Sacrifice Glen Elendra Archmage: Counter target noncreature spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryNotPredicate(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))
                        ),
                        "Target must be a noncreature spell.")
        ));
    }
}
