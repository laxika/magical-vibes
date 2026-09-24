package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCombatDamageDealersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "TMC", collectorNumber = "6")
@CardRegistration(set = "TMC", collectorNumber = "96")
public class HeroesInAHalfShell extends Card {

    public HeroesInAHalfShell() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.MUTANT, CardSubtype.NINJA, CardSubtype.TURTLE)),
                        SequenceEffect.of(
                                new PutCountersOnCombatDamageDealersEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1),
                                new DrawCardEffect(1)),
                        false,
                        true));
    }
}
