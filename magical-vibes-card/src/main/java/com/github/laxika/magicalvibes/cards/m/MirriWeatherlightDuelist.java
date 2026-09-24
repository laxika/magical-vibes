package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.CombatAttackTargetScope;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LimitOpposingBlockersThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;

@CardRegistration(set = "SLD", collectorNumber = "26")
public class MirriWeatherlightDuelist extends Card {

    public MirriWeatherlightDuelist() {
        addEffect(EffectSlot.ON_ATTACK, new LimitOpposingBlockersThisCombatEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsTapped(),
                new MaximumCombatCreaturesEffect(1, Integer.MAX_VALUE, CombatAttackTargetScope.CONTROLLER)));
    }
}
