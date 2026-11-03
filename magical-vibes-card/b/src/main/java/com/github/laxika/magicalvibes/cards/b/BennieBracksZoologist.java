package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerCreatedTokenThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "86")
@CardRegistration(set = "NCC", collectorNumber = "94")
@CardRegistration(set = "MKC", collectorNumber = "57")
public class BennieBracksZoologist extends Card {

    public BennieBracksZoologist() {
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerCreatedTokenThisTurn()),
                new DrawCardEffect());
    }
}
