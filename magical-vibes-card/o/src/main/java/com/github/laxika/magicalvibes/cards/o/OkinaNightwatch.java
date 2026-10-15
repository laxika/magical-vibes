package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreCardsInHandThanEachOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "SOK", collectorNumber = "140")
public class OkinaNightwatch extends Card {

    public OkinaNightwatch() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHasMoreCardsInHandThanEachOpponent(),
                new StaticBoostEffect(3, 3, GrantScope.SELF)));
    }
}
