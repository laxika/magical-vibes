package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeGainedThisTurn;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SeekHighestManaValueCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "1")
public class CeriseSlayerOfFear extends Card {

    public CeriseSlayerOfFear() {
        addTrigger(Triggers.secondMainPhase(ActivePlayer.YOU), new ConditionalEffect(
                new GainedLifeThisTurn(),
                new SeekHighestManaValueCardEffect(new LifeGainedThisTurn(CountScope.CONTROLLER))));
    }
}
