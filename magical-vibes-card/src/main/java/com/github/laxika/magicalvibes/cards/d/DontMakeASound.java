package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "49")
public class DontMakeASound extends Card {

    public DontMakeASound() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2, List.of(new SurveilEffect(2))));
    }
}
