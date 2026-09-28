package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CombatAttackTargetScope;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "138")
public class JudoonEnforcers extends Card {

    public JudoonEnforcers() {
        addEffect(EffectSlot.STATIC,
                new MaximumCombatCreaturesEffect(1, Integer.MAX_VALUE, CombatAttackTargetScope.CONTROLLER));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{W}",
                List.of(),
                "Suspend 6—{1}{R}{W}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(6));
    }
}
