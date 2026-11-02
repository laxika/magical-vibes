package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreCardsInHandThanEachOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOK", collectorNumber = "7")
public class DescendantOfKiyomaro extends Card {

    public DescendantOfKiyomaro() {
        ControllerHasMoreCardsInHandThanEachOpponent condition =
                new ControllerHasMoreCardsInHandThanEachOpponent();
        addStaticEffect(new ConditionalEffect(condition,
                new StaticBoostEffect(1, 2, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(condition,
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamage(),
                        new GainLifeEffect(3)), GrantScope.SELF)));
    }
}
