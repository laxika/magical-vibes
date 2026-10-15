package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EnergyCountersPaidOrLostThisTurn;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "55")
@CardRegistration(set = "M3C", collectorNumber = "107")
public class BlasterHulk extends Card {

    public BlasterHulk() {
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new EnergyCountersPaidOrLostThisTurn()));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF));
        var damage = new DealDividedDamageEffect(
                new Fixed(8), null, DivisionMode.CHOSEN, new PermanentIsCreaturePredicate(),
                8, false, false, true);
        target(TargetFilters.creature(), 0, 8);
        registerEffectTargetIndex(damage, 0);
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new EnergyCountersEffect(2),
                new ForcedCostOrElseEffect(
                        new PayEnergyCost(8), List.of(), true,
                        List.of(new QueueReflexiveAbilityEffect(damage)))));
    }
}
