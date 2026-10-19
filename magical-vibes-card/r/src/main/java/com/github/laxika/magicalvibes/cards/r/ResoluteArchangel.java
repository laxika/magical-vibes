package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.StartingLifeTotal;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeBelowStartingLife;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;

@CardRegistration(set = "M15", collectorNumber = "28")
public class ResoluteArchangel extends Card {

    public ResoluteArchangel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new ControllerLifeBelowStartingLife(),
                        new SetLifeTotalEffect(new StartingLifeTotal())));
    }
}
