package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "178")
public class SubterraneanShambler extends Card {

    public SubterraneanShambler() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.toEachCreature(1,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), DealDamageEffect.toEachCreature(1,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
        addStaticEffect(new EchoEffect("{3}{R}"));
    }
}
