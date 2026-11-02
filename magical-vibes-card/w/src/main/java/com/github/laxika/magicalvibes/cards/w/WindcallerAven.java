package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "77")
@CardRegistration(set = "CMM", collectorNumber = "133")
public class WindcallerAven extends Card {

    public WindcallerAven() {
        addTrigger(Triggers.youCycleThisCard(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET, new PermanentIsCreaturePredicate()));
        addCycling("{U}");
    }
}
