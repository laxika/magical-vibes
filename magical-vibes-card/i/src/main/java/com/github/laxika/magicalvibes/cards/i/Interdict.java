package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterAbilityAndLockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "69")
public class Interdict extends Card {

    public Interdict() {
        // "Counter target activated ability from an artifact, creature, enchantment, or land.
        //  That permanent's activated abilities can't be activated this turn. Draw a card."
        // An activated ability's stack entry carries its source card, so the permanent type filter
        // is a card-type test on an ACTIVATED_ABILITY entry. Mana abilities never use the stack,
        // so they can't be targeted. The lock is stamped on the ability's source permanent.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.ACTIVATED_ABILITY)),
                        new StackEntryCardTypeInPredicate(Set.of(
                                CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT, CardType.LAND)))),
                "Target must be an activated ability from an artifact, creature, enchantment, or land."))
                .addEffect(EffectSlot.SPELL,
                        new CounterAbilityAndLockSourceEffect(EffectDuration.UNTIL_END_OF_TURN));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
