package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
                .addSpellEffect(new EarthbendTargetLandEffect(3))
                .addSpellEffect(new GrantKeywordsToOwnCreaturesWithPowerAtMostTargetEffect(
                                Set.of(Keyword.HEXPROOF, Keyword.INDESTRUCTIBLE)));
        addSpellEffect(new GrantControllerKeywordUntilEndOfTurnEffect(Keyword.HEXPROOF));
    }
}
