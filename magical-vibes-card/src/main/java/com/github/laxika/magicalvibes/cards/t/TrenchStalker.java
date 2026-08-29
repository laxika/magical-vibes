package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "116")
public class TrenchStalker extends Card {

    public TrenchStalker() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerDrewAtLeastCardsThisTurn(2),
                new StaticBoostEffect(0, 0, Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK), GrantScope.SELF)
        ));
    }
}
