package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ModifiedCreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "26")
public class IntermediateChirography extends Card {

    public IntermediateChirography() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), inklingToken());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new ClassLevelUpEffect(2)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.youLoseLife().firstTimeEachTurn(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new ClassLevelUpEffect(3)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(new AllOf(List.of(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));

        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                new SourceCounterThreshold(2, CounterType.LEVEL),
                                new ModifiedCreatureDiedUnderYourControlThisTurn()))),
                inklingToken());
    }

    private CreateTokenEffect inklingToken() {
        return new CreateTokenEffect(
                1,
                "Inkling",
                2,
                1,
                CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK),
                List.of(CardSubtype.INKLING),
                Set.of(Keyword.FLYING),
                Set.of());
    }
}
