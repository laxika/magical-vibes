package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DMU", collectorNumber = "107")
@CardRegistration(set = "DMU", collectorNumber = "290")
public class SheoldredTheApocalypse extends Card {

    public SheoldredTheApocalypse() {
        addTrigger(Triggers.youDrawCard(), new GainLifeEffect(2));
        addTrigger(Triggers.opponentDrawsCard(), new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
