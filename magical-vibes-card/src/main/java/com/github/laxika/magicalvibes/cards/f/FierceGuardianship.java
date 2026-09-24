package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "94")
@CardRegistration(set = "CMM", collectorNumber = "489")
@CardRegistration(set = "CMM", collectorNumber = "694")
@CardRegistration(set = "TLE", collectorNumber = "307")
public class FierceGuardianship extends Card {

    public FierceGuardianship() {
        // If you control a commander, you may cast this spell without paying its mana cost.
        addCastingOption(new AlternateHandCast(List.of(), new ControllerControlsCommander(), false));

        // Counter target noncreature spell.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                "Target must be a noncreature spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
