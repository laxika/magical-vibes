package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/**
 * Superior Numbers deals damage to target creature equal to the number of creatures you control in
 * excess of the number of creatures target opponent controls.
 *
 * <p>The engine is two-player, so "target opponent" is uniquely the single opponent
 * ({@link CountScope#OPPONENTS}). "In excess of" never goes negative, hence the {@link Max} floor
 * at zero.</p>
 */
@CardRegistration(set = "MIR", collectorNumber = "244")
public class SuperiorNumbers extends Card {

    public SuperiorNumbers() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new Max(
                new Fixed(0),
                new Sum(
                        new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER),
                        new Scaled(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS), -1)))));
    }
}
