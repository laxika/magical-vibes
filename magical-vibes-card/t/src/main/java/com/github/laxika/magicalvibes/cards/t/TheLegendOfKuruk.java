package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AvatarKuruk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TLA", collectorNumber = "61")
public class TheLegendOfKuruk extends Card {

    public TheLegendOfKuruk() {
        setBackFaceCard(new AvatarKuruk());

        addEffect(EffectSlot.SAGA_CHAPTER_I,
                SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(1)));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                SequenceEffect.of(new ScryEffect(2), new DrawCardEffect(1)));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AvatarKuruk";
    }
}
