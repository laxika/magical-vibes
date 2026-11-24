package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "46")
public class PolymorphousRush extends Card {

    public PolymorphousRush() {
        setAdditionalManaCostPerExtraTarget("{1}{U}");
        target(TargetFilters.creatureYouControl(), 0, 99)
                .addSpellEffect(ChoosePermanentsEffect.exactly(1,
                        Subjects.controlledBy(Subjects.eachPlayer(), new PermanentIsCreaturePredicate()),
                        "a creature to copy"))
                .addSpellEffect(BecomeCopyOfSubjectEffect.of(Subjects.thisWay(StepKind.CHOOSE))
                        .by(Subjects.target()).untilEndOfTurn());
    }
}
