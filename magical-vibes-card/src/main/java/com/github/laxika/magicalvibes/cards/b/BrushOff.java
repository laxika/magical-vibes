package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingStackEntryEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "39")
public class BrushOff extends Card {

    public BrushOff() {
        // This spell costs {1}{U} less to cast if it targets an instant or sorcery spell.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfTargetingStackEntryEffect(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)), 2));

        // Counter target spell.
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
