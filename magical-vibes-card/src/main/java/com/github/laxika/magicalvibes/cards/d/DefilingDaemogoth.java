package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "SOC", collectorNumber = "25")
@CardRegistration(set = "SOC", collectorNumber = "75")
public class DefilingDaemogoth extends Card {

    public DefilingDaemogoth() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new GainLifeEffect(1)));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new LoseLifeEffect(new LifeGainedThisTurn(CountScope.CONTROLLER), LoseLifeRecipient.EACH_OPPONENT));
    }
}
