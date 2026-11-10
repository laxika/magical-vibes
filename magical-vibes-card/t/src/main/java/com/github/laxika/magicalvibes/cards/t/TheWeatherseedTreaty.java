package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReadAheadEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "188")
public class TheWeatherseedTreaty extends Card {

    public TheWeatherseedTreaty() {
        addStaticEffect(new ReadAheadEffect(false));
        addTrigger(Triggers.chapter(1),
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));

        addTrigger(Triggers.chapter(2), new CreateTokenEffect(
                1, "Saproling", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));

        BasicLandTypesAmongControlledLands domain = new BasicLandTypesAmongControlledLands();
        addTrigger(Triggers.chapter(3),
                new BoostTargetCreatureEffect(domain, domain),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        setSagaChapterTargetFilter(3, Set.of(TargetFilters.creatureYouControl()));
    }
}
