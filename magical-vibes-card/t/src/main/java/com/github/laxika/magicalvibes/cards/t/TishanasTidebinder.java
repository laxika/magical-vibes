package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterAbilityAndRemoveSourceAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "81")
@CardRegistration(set = "LCI", collectorNumber = "335")
public class TishanasTidebinder extends Card {

    public TishanasTidebinder() {
        // When this creature enters, counter up to one target activated or triggered ability. If an ability
        // of an artifact, creature, or planeswalker is countered this way, that permanent loses all abilities
        // for as long as this creature remains on the battlefield. (The target may be an ability of any source.)
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.ACTIVATED_ABILITY,
                                StackEntryType.TRIGGERED_ABILITY)))),
                "Target must be an activated or triggered ability."),
                0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CounterAbilityAndRemoveSourceAbilitiesEffect());
    }
}
