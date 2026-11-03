package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "20")
public class Transcendence extends Card {

    public Transcendence() {
        addStaticEffect(new CantLoseGameFromLifeEffect());

        addTrigger(Triggers.youLoseLife(),
                new GainLifeEffect(new Scaled(new EventValue(), 2)));

        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) -> gameData.getLife(controllerId) >= 20),
                new ControllerLosesGameEffect());
    }
}
