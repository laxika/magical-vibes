package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayXManaMoveCountersFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "69")
public class TesterOfTheTangential extends Card {

    public TesterOfTheTangential() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new PayXManaMoveCountersFromSourceToTargetCreatureEffect());
    }
}
