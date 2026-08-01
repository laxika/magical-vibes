package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
 * Mizzium Mortars deals 4 damage to target creature you don't control.
 * <p>
 * Overload {3}{R}{R}{R} (CR 702.96a): paying the overload cost instead of {1}{R} changes "target"
 * to "each", so the spell deals 4 damage to each creature its controller doesn't control and, per
 * CR 702.96b, chooses no targets at all.
 */
@CardRegistration(set = "RTR", collectorNumber = "101")
public class MizziumMortars extends Card {

    public MizziumMortars() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{R}{R}{R}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new DealDamageToTargetCreatureEffect(4),
                new MassDamageEffect(4, false, false,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))));
        target(TargetFilters.creatureAnOpponentControls());
    }
}
