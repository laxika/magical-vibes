package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.TargetSpellPower;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "160")
public class EssenceBacklash extends Card {

    public EssenceBacklash() {
        // Counter target creature spell. Essence Backlash deals damage equal to that spell's power
        // to its controller. Damage before the counter so the spell is still on the stack for
        // TargetSpellPower / TARGET_SPELL_CONTROLLER (rules-equivalent; also covers uncounterable).
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                "Target must be a creature spell."
        )).addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(
                new TargetSpellPower(), DamageRecipient.TARGET_SPELL_CONTROLLER))
          .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
