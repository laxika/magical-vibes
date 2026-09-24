package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "CMM", collectorNumber = "104")
public class LoyalDrake extends Card {

    public LoyalDrake() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(new ControllerControlsCommander(), new DrawCardEffect(1)));
    }
}
