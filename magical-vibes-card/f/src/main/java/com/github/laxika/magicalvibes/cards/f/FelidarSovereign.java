package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "12")
@CardRegistration(set = "BFZ", collectorNumber = "26")
@CardRegistration(set = "SLD", collectorNumber = "1182")
@CardRegistration(set = "SLD", collectorNumber = "1827")
public class FelidarSovereign extends Card {

    public FelidarSovereign() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerLifeAtLeast(40)),
                new WinGameEffect());
    }
}
