package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerGreaterThanBasePowerPredicate;

@CardRegistration(set = "LCI", collectorNumber = "232")
@CardRegistration(set = "LCI", collectorNumber = "304")
public class KutzilMalametExemplar extends Card {

    public KutzilMalametExemplar() {
        addEffect(EffectSlot.STATIC, new OpponentsCantCastOrActivateDuringYourTurnEffect(false));
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentPowerGreaterThanBasePowerPredicate(),
                        new DrawCardEffect(1),
                        false,
                        true));
    }
}
