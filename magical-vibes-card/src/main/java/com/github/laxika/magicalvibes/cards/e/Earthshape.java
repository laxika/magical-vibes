package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "67")
public class Earthshape extends Card {

    public Earthshape() {
        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.SPELL, new EarthbendTargetLandEffect(3))
                .addEffect(EffectSlot.SPELL,
                        new GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffect(
                                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE)));
        addEffect(EffectSlot.SPELL, new GrantControllerKeywordUntilEndOfTurnEffect(Keyword.HEXPROOF));
    }
}
