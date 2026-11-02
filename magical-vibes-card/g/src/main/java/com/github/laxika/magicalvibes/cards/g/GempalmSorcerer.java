package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "39")
public class GempalmSorcerer extends Card {

    public GempalmSorcerer() {
        addTrigger(Triggers.youCycleThisCard(),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.ALL_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.WIZARD)));
        addCycling("{2}{U}");
    }
}
