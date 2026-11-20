package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "228")
@CardRegistration(set = "SCD", collectorNumber = "246")
public class StaggeringInsight extends Card {

    public StaggeringInsight() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.attachedCreature()))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        new DrawCardEffect(1)),
                        Subjects.attachedCreature()));
    }
}
