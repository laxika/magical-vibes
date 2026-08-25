package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "MID", collectorNumber = "13")
public class ChaplainOfAlms extends Card {

    public ChaplainOfAlms() {
        setBackFaceCard(new ChapelShieldgeist());

        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(1));

        addCastingOption(new DisturbCast("{3}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChapelShieldgeist";
    }
}
