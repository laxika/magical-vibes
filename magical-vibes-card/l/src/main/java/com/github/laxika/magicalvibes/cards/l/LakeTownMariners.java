package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GoneFishing;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "HOB", collectorNumber = "44")
public class LakeTownMariners extends Card {

    public LakeTownMariners() {
        setBackFaceCard(new GoneFishing());
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));
        addCastingOption(new AdventureCast("{3}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GoneFishing";
    }
}
