package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "188")
public class TheWeatherseedTreaty extends Card {

    public TheWeatherseedTreaty() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                1, "Saproling", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));

        BasicLandTypesAmongControlledLands domain = new BasicLandTypesAmongControlledLands();
        addEffect(EffectSlot.SAGA_CHAPTER_III, new BoostTargetCreatureEffect(domain, domain));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(TargetFilters.creatureYouControl()));
    }
}
