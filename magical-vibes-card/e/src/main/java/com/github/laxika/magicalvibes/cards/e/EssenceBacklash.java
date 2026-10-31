package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
        // to its controller. Effects run in printed order: the counter first, then the damage, which
        // reads the countered spell's last-known power and controller.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                "Target must be a creature spell."
        )).addSpellEffect(new CounterSpellEffect())
          .addSpellEffect(new DealDamageToPlayersEffect(
                new TargetSpellPower(), DamageRecipient.TARGET_SPELL_CONTROLLER));
    }
}
