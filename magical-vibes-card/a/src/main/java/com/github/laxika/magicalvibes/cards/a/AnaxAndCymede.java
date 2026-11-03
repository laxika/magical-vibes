package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "186")
@CardRegistration(set = "DDL", collectorNumber = "11")
@CardRegistration(set = "PIO", collectorNumber = "202")
public class AnaxAndCymede extends Card {

    public AnaxAndCymede() {
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                new BoostAllOwnCreaturesEffect(1, 1),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_OWN_CREATURES));
    }
}
