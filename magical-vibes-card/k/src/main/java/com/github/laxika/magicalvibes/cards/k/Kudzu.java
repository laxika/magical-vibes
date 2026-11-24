package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SUM", collectorNumber = "205")
@CardRegistration(set = "LEB", collectorNumber = "205")
@CardRegistration(set = "3ED", collectorNumber = "205")
@CardRegistration(set = "ME4", collectorNumber = "159")
@CardRegistration(set = "2ED", collectorNumber = "205")
@CardRegistration(set = "LEA", collectorNumber = "204")
public class Kudzu extends Card {

    public Kudzu() {
        target(TargetFilters.land());
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                SequenceEffect.of(
                        new DestroyTargetPermanentEffect(Subjects.triggering()),
                        new MayEffect(
                                SequenceEffect.of(
                                        ChoosePermanentsEffect.exactly(1,
                                                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentIsLandPredicate()), "a land")
                                        .chosenBy(Subjects.controllerOf(Subjects.triggering()))
                                        .thatCanHost(Subjects.source()),
                                        new AttachSourceEquipmentToTargetCreatureEffect(Subjects.source(),
                                                Subjects.thisWay(StepKind.CHOOSE))),
                                "Attach Kudzu to a land?",
                                null,
                                MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)));
    }
}
