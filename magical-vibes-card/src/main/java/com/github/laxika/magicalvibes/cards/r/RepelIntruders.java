package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "147")
public class RepelIntruders extends Card {

    public RepelIntruders() {
        // Create two 1/1 white Kithkin Soldier creature tokens if {W} was spent to cast this spell.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.WHITE),
                new CreateTokenEffect(
                        2, "Kithkin Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.KITHKIN, CardSubtype.SOLDIER),
                        Set.of(), Set.of())));

        // Counter up to one target creature spell if {U} was spent to cast this spell.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                "Target must be a creature spell."
        ), 0, 1).addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLUE),
                new CounterSpellEffect()));
    }
}
