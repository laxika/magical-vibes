package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerGreaterThanBasePowerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "232")
@CardRegistration(set = "LCI", collectorNumber = "304")
public class KutzilMalametExemplar extends Card {

    public KutzilMalametExemplar() {
        addStaticEffect(new OpponentsCantCastOrActivateDuringYourTurnEffect(false));
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(
                        new PermanentPowerGreaterThanBasePowerPredicate(),
                        new DrawCardEffect(1),
                        false,
                        true));
    }
}
