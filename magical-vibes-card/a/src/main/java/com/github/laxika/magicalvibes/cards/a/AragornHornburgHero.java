package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsRenownedPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "492")
@CardRegistration(set = "LTC", collectorNumber = "536")
public class AragornHornburgHero extends Card {

    public AragornHornburgHero() {
        var attacking = new PermanentIsAttackingPredicate();

        addStaticEffect(new GrantKeywordEffect(
                Keyword.FIRST_STRIKE, GrantScope.ALL_OWN_CREATURES, attacking));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                new RenownEffect(1)), GrantScope.ALL_OWN_CREATURES, attacking));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsRenownedPredicate()),
                AllyCombatDamageTriggerEffect.boundToDealer(new DoublePlusOneCountersOnSourceEffect()));
    }
}
