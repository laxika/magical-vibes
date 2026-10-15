package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "SCG", collectorNumber = "37")
public class HinderingTouch extends Card {

    public HinderingTouch() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
