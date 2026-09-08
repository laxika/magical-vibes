package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "59")
public class HurlIntoHistory extends Card {

    public HurlIntoHistory() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT, CardType.CREATURE)),
                        new StackEntryNotPredicate(new StackEntryTypeInPredicate(
                                Set.of(StackEntryType.TRIGGERED_ABILITY, StackEntryType.ACTIVATED_ABILITY)))
                )),
                "Target must be an artifact or creature spell."
        ))
                // Discover first so the target spell's mana value is still available.
                .addEffect(EffectSlot.SPELL, new DiscoverEffect(new TargetSpellManaValue()))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
