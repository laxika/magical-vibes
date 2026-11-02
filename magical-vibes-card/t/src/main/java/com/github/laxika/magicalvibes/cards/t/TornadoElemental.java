package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "97")
@CardRegistration(set = "HOP", collectorNumber = "79")
@CardRegistration(set = "C14", collectorNumber = "222")
@CardRegistration(set = "CMA", collectorNumber = "159")
public class TornadoElemental extends Card {

    public TornadoElemental() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MassDamageEffect(6, false, false, new PermanentHasKeywordPredicate(Keyword.FLYING)));
        addStaticEffect(new AssignCombatDamageAsThoughUnblockedEffect());
    }
}
