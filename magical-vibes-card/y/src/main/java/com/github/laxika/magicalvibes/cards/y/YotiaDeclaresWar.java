package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReadAheadEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCanBecomeTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "153")
public class YotiaDeclaresWar extends Card {

    public YotiaDeclaresWar() {
        addStaticEffect(new ReadAheadEffect(false));

        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                1, "Ornithopter", 0, 2, null,
                List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)));

        // "When you do" triggers even when no artifact was tapped, and deals 0 damage (the card's ruling).
        addTrigger(Triggers.chapter(2), SequenceEffect.of(
                ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(),
                                new PermanentAllOfPredicate(List.of(new PermanentIsArtifactPredicate(),
                                        new PermanentCanBecomeTappedPredicate()))),
                        "any number of untapped artifacts you control to tap"),
                new TapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)),
                QueueReflexiveAbilityEffect.withEventValue(
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(new EventValue()),
                        new AmountThisWay(StepKind.TAP))));

        addTrigger(Triggers.chapter(3), new AnimatePermanentsEffect(
                4, 4, List.of(), Set.of(), null, Set.of(), Subjects.target(),
                Duration.UNTIL_END_OF_TURN));
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(), "Target must be an artifact you control"), 0, 1)));
    }
}
