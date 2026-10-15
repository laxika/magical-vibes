package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "M21", collectorNumber = "81")
public class TomeAnima extends Card {

    public TomeAnima() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerDrewAtLeastCardsThisTurn(2),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
    }
}
