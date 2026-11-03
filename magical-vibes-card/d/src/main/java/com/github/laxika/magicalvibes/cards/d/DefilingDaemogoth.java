package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "25")
@CardRegistration(set = "SOC", collectorNumber = "75")
public class DefilingDaemogoth extends Card {

    public DefilingDaemogoth() {
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), new GainLifeEffect(1));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new LoseLifeEffect(new LifeGainedThisTurn(CountScope.CONTROLLER), LoseLifeRecipient.EACH_OPPONENT));
    }
}
