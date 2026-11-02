package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "38")
@CardRegistration(set = "V16", collectorNumber = "10")
public class NearDeathExperience extends Card {

    public NearDeathExperience() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new AllOf(List.of(
                        new ControllerLifeAtLeast(1),
                        new ControllerLifeAtMost(1))),
                new WinGameEffect()));
    }
}
