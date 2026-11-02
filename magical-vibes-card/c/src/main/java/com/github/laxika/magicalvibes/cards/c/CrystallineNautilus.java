package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "34")
public class CrystallineNautilus extends Card {

    public CrystallineNautilus() {
        addCastingOption(new BestowCast("{3}{U}{U}"));

        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new SacrificeSelfEffect());

        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(4, 4, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.becomesTargetOfSpellOrAbility(),
                        new SacrificeSelfEffect()), GrantScope.ENCHANTED_CREATURE));
    }
}
