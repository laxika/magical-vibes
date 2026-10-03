package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CastSpellsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "13")
public class AdvancedFloralInvocations extends Card {

    public AdvancedFloralInvocations() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MillEffect(2, MillRecipient.CONTROLLER), TriggerMode.INDEPENDENT);

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B/G}",
                List.of(new ClassLevelUpEffect(2)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B/G}{B/G}",
                List.of(new ClassLevelUpEffect(3)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(new AllOf(List.of(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));

        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new PerpetuallyBoostCreatureCardsInGraveyardEffect(1, 1)), TriggerMode.INDEPENDENT);

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(2, CounterType.LEVEL),
                new PlayLandsFromGraveyardEffect()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(2, CounterType.LEVEL),
                new CastSpellsFromGraveyardEffect(
                        new CardTypePredicate(CardType.CREATURE), List.of(), CounterType.FINALITY)));
    }
}
