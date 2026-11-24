package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "65")
@CardRegistration(set = "FIC", collectorNumber = "158")
@CardRegistration(set = "FIC", collectorNumber = "470")
public class YuffieMateriaHunter extends Card {

    public YuffieMateriaHunter() {
        addNinjutsu("{1}{R}");

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()))),
                "Target must be a noncreature artifact"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                        new GainControlOfTargetEffect(Duration.WHILE_SOURCE_ON_BATTLEFIELD),
                        new MayEffect(SequenceEffect.of(
                                        ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(),
                                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)), "an Equipment you control")
                                        .thatCanBeAttachedTo(Subjects.source()),
                                        new AttachSourceEquipmentToTargetCreatureEffect(Subjects.thisWay(StepKind.CHOOSE), Subjects.source())),
                                "Attach an Equipment you control to Yuffie?")));
    }
}
