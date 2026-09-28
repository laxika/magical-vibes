package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CantAttackWithOddNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MB2", collectorNumber = "269")
@CardRegistration(set = "MB2", collectorNumber = "505")
public class RuleWithAnEvenHand extends Card {

    public RuleWithAnEvenHand() {
        addEffect(EffectSlot.COMMAND_ZONE_STATIC, new CantAttackWithOddNumberOfCreaturesEffect());
        target(TargetFilters.creature()).addEffect(EffectSlot.COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK,
                new DoubleTargetCreaturePowerEffect(new Fixed(1)));
    }
}
