package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "LGN", collectorNumber = "56")
public class WarpedResearcher extends Card {

    public WarpedResearcher() {
        addTrigger(Triggers.playerCyclesCard(),
                new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.SHROUD), GrantScope.SELF));
    }
}
