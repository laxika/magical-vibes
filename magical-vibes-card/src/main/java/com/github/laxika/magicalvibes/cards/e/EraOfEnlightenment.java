package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HandOfEnlightenment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "NEO", collectorNumber = "11")
public class EraOfEnlightenment extends Card {

    public EraOfEnlightenment() {
        setBackFaceCard(new HandOfEnlightenment());

        addEffect(EffectSlot.SAGA_CHAPTER_I, new ScryEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "HandOfEnlightenment";
    }
}
