package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "223")
public class SteamVines extends Card {

    public SteamVines() {
        target(TargetFilters.land());
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                SequenceEffect.of(
                        new DestroyTargetPermanentEffect(Subjects.triggering()),
                        DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), 1),
                        ChoosePermanentsEffect.exactly(1,
                                                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentIsLandPredicate()), "a land")
                                        .chosenBy(Subjects.controllerOf(Subjects.triggering()))
                                        .thatCanHost(Subjects.source()),
                        new AttachSourceEquipmentToTargetCreatureEffect(Subjects.source(), Subjects.thisWay(StepKind.CHOOSE))));
    }
}
