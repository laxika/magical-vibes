package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HML", collectorNumber = "79")
@CardRegistration(set = "ME2", collectorNumber = "148")
public class Retribution extends Card {

    public Retribution() {
        // "Choose two target creatures controlled by the same opponent. That player chooses and
        // sacrifices one of those creatures. Put a -1/-1 counter on the other."
        // This engine is strictly 2-player, so "an opponent controls" on both target groups already
        // means the same opponent controls both.
        target(TargetFilters.creatureAnOpponentControls())
                .addSpellEffect(ChoosePermanentsEffect.exactly(1, Subjects.allTargets(),
                                "a creature to sacrifice; the other gets a -1/-1 counter")
                        .chosenBy(Subjects.controllerOf(Subjects.allTargets())))
                .addSpellEffect(new SacrificeSelfEffect(Subjects.thisWay(StepKind.CHOOSE),
                        Subjects.controllerOf(Subjects.thisWay(StepKind.CHOOSE))))
                .addSpellEffect(new PutCounterOnTargetPermanentEffect(
                        Subjects.except(Subjects.allTargets(), Subjects.thisWay(StepKind.CHOOSE)),
                        CounterType.MINUS_ONE_MINUS_ONE));
        target(TargetFilters.creatureAnOpponentControls());
    }
}
