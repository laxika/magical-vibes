package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreCardsInHandThanEachOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SOK", collectorNumber = "7")
public class DescendantOfKiyomaro extends Card {

    public DescendantOfKiyomaro() {
        ControllerHasMoreCardsInHandThanEachOpponent condition =
                new ControllerHasMoreCardsInHandThanEachOpponent();
        addEffect(EffectSlot.STATIC, new ConditionalEffect(condition,
                new StaticBoostEffect(1, 2, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(condition,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE,
                        new GainLifeEffect(3), GrantScope.SELF)));
    }
}
