package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "124")
@CardRegistration(set = "PIP", collectorNumber = "191")
@CardRegistration(set = "PIP", collectorNumber = "719")
public class StickyFingers extends Card {

    public StickyFingers() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, Subjects.attachedCreature()))
                .addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(
                        Triggers.dealsCombatDamageToPlayer(), CreateTokenEffect.ofTreasureToken(1)),
                        Subjects.attachedCreature()))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED), new DrawCardEffect());
    }
}
