package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "147")
public class FanaticOfXenagos extends Card {

    public FanaticOfXenagos() {
        addStaticEffect(new TributeEffect(1));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new TributeNotPaidEffect(new BoostSelfEffect(1, 1)));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new TributeNotPaidEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
    }
}
