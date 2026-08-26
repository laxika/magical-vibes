package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreaturesDealPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SPM", collectorNumber = "120")
public class TerrificTeamUp extends Card {

    public TerrificTeamUp() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentMinManaValuePredicate(4)),
                new ReduceOwnCastCostEffect(new Fixed(2))));

        var victimTarget = target(TargetFilters.creatureAnOpponentControls());
        target(TargetFilters.creatureYouControl(), 1, 2)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 0));
        victimTarget.addEffect(EffectSlot.SPELL, new TargetCreaturesDealPowerDamageToTargetEffect(1, 0));
    }
}
