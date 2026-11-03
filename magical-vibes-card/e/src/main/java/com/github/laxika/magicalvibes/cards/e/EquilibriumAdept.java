package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "106")
public class EquilibriumAdept extends Card {

    public EquilibriumAdept() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopCardsMayPlayUntilNextTurnEffect(1));
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF));
    }
}
