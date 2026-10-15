package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingStackEntryEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "69")
public class OutOfAir extends Card {

    public OutOfAir() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingStackEntryEffect(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)), 2));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
