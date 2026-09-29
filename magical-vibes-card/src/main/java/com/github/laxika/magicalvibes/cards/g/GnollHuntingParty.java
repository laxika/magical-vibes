package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.CreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "HBG", collectorNumber = "55")
public class GnollHuntingParty extends Card {

    public GnollHuntingParty() {
        addEffect(EffectSlot.STATIC,
                new ReduceOwnCastCostEffect(new CreaturesAttackedThisTurn(CountScope.CONTROLLER)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)));
    }
}
