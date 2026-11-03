package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "35")
public class CuriousObsession extends Card {

    public CuriousObsession() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        new MayEffect(new DrawCardEffect(1), "Draw a card?")),
                        GrantScope.ENCHANTED_CREATURE));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new AttackedWithCreaturesThisTurn(1))),
                new SacrificeSelfEffect());
    }
}
