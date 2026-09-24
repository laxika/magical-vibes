package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;

@CardRegistration(set = "2XM", collectorNumber = "75")
@CardRegistration(set = "C14", collectorNumber = "20")
public class WellOfIdeas extends Card {

    public WellOfIdeas() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(2));
        addEffect(EffectSlot.EACH_DRAW_TRIGGERED, DrawCardForTargetPlayerEffect.forOpponentDrawStep(1));
        addEffect(EffectSlot.DRAW_TRIGGERED, new DrawCardEffect(2));
    }
}
