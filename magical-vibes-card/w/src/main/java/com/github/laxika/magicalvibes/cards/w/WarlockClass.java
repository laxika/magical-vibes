package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToLifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "125")
public class WarlockClass extends Card {

    public WarlockClass() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Morbid()),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new ClassLevelUpEffect(2,
                        List.of(LookAtTopCardsEffect.chooseNToHandRestToGraveyard(3, 1)))),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}{B}",
                List.of(new ClassLevelUpEffect(3)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(new AllOf(List.of(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(2, CounterType.LEVEL)),
                new EachOpponentLosesLifeEqualToLifeLostThisTurn());
    }
}
