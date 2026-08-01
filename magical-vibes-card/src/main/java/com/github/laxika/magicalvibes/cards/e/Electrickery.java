package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Electrickery deals 1 damage to target creature you don't control.
 * <p>
 * Overload {1}{R} (CR 702.96a): paying the overload cost instead of {R} changes "target" to
 * "each", so the spell deals 1 damage to every creature its controller doesn't control and,
 * per CR 702.96b, chooses no targets at all.
 */
@CardRegistration(set = "RTR", collectorNumber = "93")
public class Electrickery extends Card {

    public Electrickery() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new DealDamageToTargetCreatureEffect(1),
                new MassDamageEffect(new Fixed(1), false, false,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
        target(TargetFilters.creatureAnOpponentControls());
    }
}
