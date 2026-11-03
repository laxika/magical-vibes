package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "22")
public class AlchemistsTalent extends Card {

    public AlchemistsTalent() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofTappedTreasureToken(2));

        ActivatedAbility treasureManaAbility = new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect(2)),
                "{T}, Sacrifice this artifact: Add two mana of any one color."
        );

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new ClassLevelUpEffect(2)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new GrantActivatedAbilityEffect(
                        treasureManaAbility,
                        GrantScope.OWN_PERMANENTS,
                        new PermanentHasSubtypePredicate(CardSubtype.TREASURE))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new ClassLevelUpEffect(3)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));

        addTrigger(Triggers.youCastSpell().paidWithTreasureMana(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(2, CounterType.LEVEL)),
                new DealDamageToPlayersEffect(new TargetSpellManaValue(), DamageRecipient.EACH_OPPONENT));
    }
}
