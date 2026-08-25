package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "20")
public class Transcendence extends Card {

    public Transcendence() {
        addEffect(EffectSlot.STATIC, new CantLoseGameFromLifeEffect());

        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE,
                new GainLifeEffect(new Scaled(new EventValue(), 2)));

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.getLife(controllerId) >= 20,
                List.of(new ControllerLosesGameEffect()),
                "Transcendence's state-triggered ability"));
    }
}
