package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FCA", collectorNumber = "50")
@CardRegistration(set = "C16", collectorNumber = "30")
public class BruseTarlBoorishHerder extends Card {

    public BruseTarlBoorishHerder() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET))
                .addTrigger(Triggers.attacks(),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET),
                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET));
    }
}
