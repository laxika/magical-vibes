package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOB", collectorNumber = "31")
public class VowToErebor extends Card {

    public VowToErebor() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(BoostEffect.target(2, 2))
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.DWARF)),
                        new MayEffect(
                                SequenceEffect.of(
                                        ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(),
                                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)), "an Equipment you control")
                                        .thatCanBeAttachedTo(Subjects.target()),
                                        new AttachSourceEquipmentToTargetCreatureEffect(Subjects.thisWay(StepKind.CHOOSE), Subjects.target())),
                                "Attach an Equipment you control to that creature?")));
    }
}
