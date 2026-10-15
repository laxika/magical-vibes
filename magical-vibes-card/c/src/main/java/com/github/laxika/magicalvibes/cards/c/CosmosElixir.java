package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "KHM", collectorNumber = "237")
@CardRegistration(set = "KHM", collectorNumber = "368")
public class CosmosElixir extends Card {

    public CosmosElixir() {
        ControllerLifeAtLeast condition = new ControllerLifeAtLeast(1, true);
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalReplacementEffect(condition, new GainLifeEffect(2), new DrawCardEffect()));
    }
}
