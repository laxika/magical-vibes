package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsOwnCommander;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "104")
@CardRegistration(set = "C18", collectorNumber = "10")
public class LoyalDrake extends Card {

    public LoyalDrake() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerControlsOwnCommander()),
                new DrawCardEffect(1));
    }
}
