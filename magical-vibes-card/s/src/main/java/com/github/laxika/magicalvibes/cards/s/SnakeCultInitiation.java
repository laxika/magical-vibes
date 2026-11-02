package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "89")
public class SnakeCultInitiation extends Card {

    public SnakeCultInitiation() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.POISONOUS, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                                new GivePoisonCountersEffect(3, PoisonRecipient.TARGET_PLAYER)),
                                GrantScope.ENCHANTED_CREATURE));
    }
}
