package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DireStrainDemolisher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "MID", collectorNumber = "174")
public class BurlyBreaker extends Card {

    public BurlyBreaker() {
        setBackFaceCard(new DireStrainDemolisher());

        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(1));
    }

    @Override
    public String getBackFaceClassName() {
        return "DireStrainDemolisher";
    }
}
