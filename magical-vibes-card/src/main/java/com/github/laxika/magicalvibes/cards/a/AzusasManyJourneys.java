package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LikenessOfTheSeeker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

@CardRegistration(set = "NEO", collectorNumber = "172")
public class AzusasManyJourneys extends Card {

    public AzusasManyJourneys() {
        setBackFaceCard(new LikenessOfTheSeeker());

        addEffect(EffectSlot.SAGA_CHAPTER_I, new PlayAdditionalLandsEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new GainLifeEffect(3));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "LikenessOfTheSeeker";
    }
}
