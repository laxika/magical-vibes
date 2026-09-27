package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.CombatAttackTargetScope;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.LimitOpposingBlockersThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;

@CardRegistration(set = "SLD", collectorNumber = "26")
@CardRegistration(set = "SPG", collectorNumber = "15")
@CardRegistration(set = "CMM", collectorNumber = "347")
@CardRegistration(set = "CMM", collectorNumber = "585")
public class MirriWeatherlightDuelist extends Card {

    public MirriWeatherlightDuelist() {
        // Whenever Mirri attacks, each opponent can't block with more than one creature this combat.
        addEffect(EffectSlot.ON_ATTACK,
                new GrantEffectToSourceUntilEndOfCombatEffect(
                        EffectSlot.STATIC,
                        new MaximumCombatCreaturesEffect(Integer.MAX_VALUE, 1)));

        // As long as Mirri is tapped, no more than one creature can attack you each combat.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsTapped(),
                new MaximumCombatCreaturesEffect(1, Integer.MAX_VALUE, CombatAttackTargetScope.CONTROLLER)));
    }
}
