package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackPlayerAlreadyAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPG", collectorNumber = "33")
@CardRegistration(set = "LCC", collectorNumber = "230")
public class PortRazer extends Card {

    public PortRazer() {
        addStaticEffect(new CantAttackPlayerAlreadyAttackedThisTurnEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()),
                new AdditionalCombatPhaseEffect(1)));
    }
}
