package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "134")
public class InstillFuror extends Card {

    public InstillFuror() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU),
                        new ConditionalEffect(new DidntAttack(), new SacrificeSelfEffect())),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
