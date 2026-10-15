package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.n.NamelessConqueror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEO", collectorNumber = "162")
public class TheShatteredStatesEra extends Card {

    public TheShatteredStatesEra() {
        setBackFaceCard(new NamelessConqueror());

        target(TargetFilters.creature())
                .addEffect(EffectSlot.SAGA_CHAPTER_I,
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addEffect(EffectSlot.SAGA_CHAPTER_I,
                        new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SAGA_CHAPTER_I,
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new BoostAllOwnCreaturesEffect(1, 0));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "NamelessConqueror";
    }
}
