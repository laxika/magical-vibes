package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerGreaterThanBasePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "744")
public class MsMarvelElasticAlly extends Card {

    public MsMarvelElasticAlly() {
        // When Ms. Marvel enters, target creature gets +2/+0 until end of turn.
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new BoostTargetCreatureEffect(2, 0));

        // Whenever a creature you control with power greater than its base power deals combat
        // damage to a player, draw a card. This ability triggers only once each turn.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new OncePerTurnTriggerEffect(new AllyCombatDamageTriggerEffect(
                        new PermanentPowerGreaterThanBasePowerPredicate(),
                        new DrawCardEffect(1))));
    }
}
