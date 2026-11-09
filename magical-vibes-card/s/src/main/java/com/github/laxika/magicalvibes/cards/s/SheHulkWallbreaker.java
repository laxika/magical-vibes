package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "60")
@CardRegistration(set = "MSC", collectorNumber = "372")
public class SheHulkWallbreaker extends Card {

    public SheHulkWallbreaker() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.TRAMPLE,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.HERO)));
        addTrigger(Triggers.creatureYouControlBecomesBlocked()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.HERO)),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE,
                        new CreaturesBlockingSource(true)));
    }
}
