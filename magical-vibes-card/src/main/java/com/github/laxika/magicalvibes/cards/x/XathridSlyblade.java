package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "124")
public class XathridSlyblade extends Card {

    public XathridSlyblade() {
        // {3}{B}: Until end of turn, Xathrid Slyblade loses hexproof and gains first strike and deathtouch.
        addActivatedAbility(new ActivatedAbility(false, "{3}{B}",
                List.of(new RemoveKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF),
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.DEATHTOUCH), GrantScope.SELF)),
                "{3}{B}: Until end of turn, Xathrid Slyblade loses hexproof and gains first strike and deathtouch."));
    }
}
