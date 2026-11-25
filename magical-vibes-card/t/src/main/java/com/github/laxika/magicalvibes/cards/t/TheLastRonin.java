package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.StepKind;

@CardRegistration(set = "TMT", collectorNumber = "154")
public class TheLastRonin extends Card {

    public TheLastRonin() {
        addTrigger(Triggers.chapter(1),
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));

        addTrigger(Triggers.chapter(2), SequenceEffect.of(new MillEffect(4, Subjects.you()),
                new ConditionalEffect(HappenedThisWay.atLeast(StepKind.MILL, 4),
                        new QueueReflexiveAbilityEffect(ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .build()))));

        addTrigger(Triggers.chapter(3), new RegisterGlobalTriggeredAbilityEffect(
                new TriggeredAbility(
                        Triggers.creatureAttacks().withFilter(new PermanentControlledBySourceControllerPredicate()),
                        TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                        SequenceEffect.of(
                                new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                                new GrantKeywordEffect(Set.of(
                                        Keyword.TRAMPLE,
                                        Keyword.LIFELINK,
                                        Keyword.INDESTRUCTIBLE), Subjects.triggering())))));
    }
}
