package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerLostLifeLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "12")
public class FirstResponse extends Card {

    public FirstResponse() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new ConditionalEffect(
                new ControllerLostLifeLastTurn(),
                CreateTokenEffect.whiteSoldier(1)));
    }
}
