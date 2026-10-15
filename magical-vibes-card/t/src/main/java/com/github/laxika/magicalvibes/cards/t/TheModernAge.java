package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VectorGlider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;

@CardRegistration(set = "NEO", collectorNumber = "66")
public class TheModernAge extends Card {

    public TheModernAge() {
        setBackFaceCard(new VectorGlider());

        addEffect(EffectSlot.SAGA_CHAPTER_I, new DrawCardEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_I, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DrawCardEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "VectorGlider";
    }
}
