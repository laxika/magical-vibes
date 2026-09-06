package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AvatarRoku;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "TLA", collectorNumber = "145")
public class TheLegendOfRoku extends Card {

    public TheLegendOfRoku() {
        setBackFaceCard(new AvatarRoku());

        addEffect(EffectSlot.SAGA_CHAPTER_I, new ExileTopCardsMayPlayUntilNextTurnEffect(3));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new AwardAnyColorManaEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AvatarRoku";
    }
}
