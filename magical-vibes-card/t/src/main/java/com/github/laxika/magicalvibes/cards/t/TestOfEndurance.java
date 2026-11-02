package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "29")
@CardRegistration(set = "SLD", collectorNumber = "1840")
@CardRegistration(set = "DMR", collectorNumber = "32")
public class TestOfEndurance extends Card {

    public TestOfEndurance() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ConditionalEffect(new ControllerLifeAtLeast(50), new WinGameEffect()));
    }
}
