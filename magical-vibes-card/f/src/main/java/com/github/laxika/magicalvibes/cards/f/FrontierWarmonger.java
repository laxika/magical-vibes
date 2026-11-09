package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "224")
@CardRegistration(set = "LTC", collectorNumber = "217")
@CardRegistration(set = "MKC", collectorNumber = "154")
@CardRegistration(set = "C20", collectorNumber = "52")
public class FrontierWarmonger extends Card {

    public FrontierWarmonger() {
        addTrigger(Triggers.youAttack().withFilter(new PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate()),
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.DECLARED_ATTACKERS));
    }
}
