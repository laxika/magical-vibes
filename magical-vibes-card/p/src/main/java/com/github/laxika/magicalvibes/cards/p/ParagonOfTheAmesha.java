package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "14")
public class ParagonOfTheAmesha extends Card {

    public ParagonOfTheAmesha() {
        addActivatedAbility(new ActivatedAbility(false, "{W}{U}{B}{R}{G}", List.of(
                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.ANGEL, Subjects.source()),
                new BoostSelfEffect(3, 3),
                new GrantKeywordEffect(Keyword.FLYING, Subjects.source()),
                new GrantKeywordEffect(Keyword.LIFELINK, Subjects.source())),
                "{W}{U}{B}{R}{G}: Until end of turn, this creature becomes an Angel, gets +3/+3, and gains flying and lifelink."));
    }
}
