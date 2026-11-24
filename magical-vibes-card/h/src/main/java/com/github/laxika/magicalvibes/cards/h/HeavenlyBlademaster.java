package com.github.laxika.magicalvibes.cards.h;

import java.util.List;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GN3", collectorNumber = "11")
@CardRegistration(set = "CMM", collectorNumber = "28")
@CardRegistration(set = "CMM", collectorNumber = "461")
@CardRegistration(set = "C18", collectorNumber = "3")
public class HeavenlyBlademaster extends Card {

    public HeavenlyBlademaster() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(),
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentHasSubtypePredicate(CardSubtype.AURA),
                                                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)))),
                                        "Auras and Equipment you control")
                                .thatCanBeAttachedTo(Subjects.source()),
                        new AttachSourceEquipmentToTargetCreatureEffect(Subjects.thisWay(StepKind.CHOOSE), Subjects.source())));
        AttachmentsOnSource attachments = new AttachmentsOnSource(true, true);
        addStaticEffect(new DynamicStaticBoostEffect(attachments, attachments,
                Subjects.otherCreaturesYouControl()));
    }
}
