package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreCardsInHandThanEachOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "54")
public class Secretkeeper extends Card {

    public Secretkeeper() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHasMoreCardsInHandThanEachOpponent(),
                new StaticBoostEffect(2, 2, Set.of(Keyword.FLYING), GrantScope.SELF)));
    }
}
