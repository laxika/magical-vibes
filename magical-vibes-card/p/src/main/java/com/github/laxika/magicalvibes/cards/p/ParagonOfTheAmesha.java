package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "14")
public class ParagonOfTheAmesha extends Card {

    public ParagonOfTheAmesha() {
        addActivatedAbility(new ActivatedAbility(false, "{W}{U}{B}{R}{G}", List.of(
                new SourceBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.ANGEL),
                new BoostSelfEffect(3, 3),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)),
                "{W}{U}{B}{R}{G}: Until end of turn, this creature becomes an Angel, gets +3/+3, and gains flying and lifelink."));
    }
}
