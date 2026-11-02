package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "102")
public class RisenNecroregent extends Card {

    public RisenNecroregent() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                ConditionalEffect.atTriggerTime(new MaxSpeed(), CreateTokenEffect.blackZombie(1)));
    }
}
