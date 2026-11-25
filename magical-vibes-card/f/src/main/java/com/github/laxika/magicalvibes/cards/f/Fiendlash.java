package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "221")
@CardRegistration(set = "AFC", collectorNumber = "31")
@CardRegistration(set = "HBG", collectorNumber = "182")
public class Fiendlash extends Card {

    public Fiendlash() {
        addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.REACH, Subjects.attachedCreature()));

        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker"))
                .addTrigger(Triggers.enchantedCreatureDealtDamage(),
                        DealDamageEffect.toAnyTarget(new SourcePower()).byTriggeringPermanent());

        addActivatedAbility(new EquipActivatedAbility("{2}{R}"));
    }
}
