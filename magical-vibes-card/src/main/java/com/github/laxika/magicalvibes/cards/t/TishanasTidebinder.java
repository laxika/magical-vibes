package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterAbilityAndRemoveSourceAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "81")
public class TishanasTidebinder extends Card {

    public TishanasTidebinder() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(
                                StackEntryType.ACTIVATED_ABILITY,
                                StackEntryType.TRIGGERED_ABILITY)),
                        new StackEntryCardTypeInPredicate(Set.of(
                                CardType.ARTIFACT, CardType.CREATURE, CardType.PLANESWALKER)))),
                "Target must be an activated or triggered ability from an artifact, creature, or planeswalker."),
                0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CounterAbilityAndRemoveSourceAbilitiesEffect());
    }
}
