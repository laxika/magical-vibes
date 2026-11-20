package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "81")
@CardRegistration(set = "LCI", collectorNumber = "335")
public class TishanasTidebinder extends Card {

    public TishanasTidebinder() {
        // When this creature enters, counter up to one target activated or triggered ability. If an ability
        // of an artifact, creature, or planeswalker is countered this way, that permanent loses all abilities
        // for as long as this creature remains on the battlefield. (The target may be an ability of any source.)
        target(new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(StackEntryType.ABILITIES))),
                "Target must be an activated or triggered ability."),
                0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                        new CounterSpellEffect(),
                        new ConditionalEffect(HappenedThisWay.countered(), new LosesAllAbilitiesEffect(
                                Subjects.targetAbilitySource(new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsPlaneswalkerPredicate()))),
                                Duration.WHILE_SOURCE_REMAINS))));
    }
}
