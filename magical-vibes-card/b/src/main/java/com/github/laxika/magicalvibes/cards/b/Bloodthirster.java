package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackPlayerAlreadyAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "73")
public class Bloodthirster extends Card {

    public Bloodthirster() {
        addStaticEffect(new CantAttackPlayerAlreadyAttackedThisTurnEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new UntapPermanentsEffect(TapUntapScope.SELF), new AdditionalCombatPhaseEffect(1)));
    }
}
