package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.ControllerSurveiledThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "164")
public class DarkbladeAgent extends Card {

    public DarkbladeAgent() {
        ControllerSurveiledThisTurn surveiledThisTurn = new ControllerSurveiledThisTurn();
        addStaticEffect(new ConditionalEffect(
                surveiledThisTurn,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                surveiledThisTurn,
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        new DrawCardEffect(1)),
                        GrantScope.SELF)));
    }
}
