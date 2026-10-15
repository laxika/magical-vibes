package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerGreaterThanBasePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "744")
public class MsMarvelElasticAlly extends Card {

    public MsMarvelElasticAlly() {
        // When Ms. Marvel enters, target creature gets +2/+0 until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostTargetCreatureEffect(2, 0));

        // Whenever a creature you control with power greater than its base power deals combat
        // damage to a player, draw a card. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new OncePerTurnTriggerEffect(new AllyCombatDamageTriggerEffect(
                        new PermanentPowerGreaterThanBasePowerPredicate(),
                        new DrawCardEffect(1))));
    }
}
