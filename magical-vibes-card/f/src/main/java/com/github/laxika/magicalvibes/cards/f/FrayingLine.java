package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "257")
public class FrayingLine extends Card {

    public FrayingLine() {
        // When this artifact enters, put a rope counter on target creature you control.
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new PutCountersEffect(CounterType.ROPE));

        // Each upkeep, the active player may pay {2} to put a rope counter on one of their
        // creatures. Otherwise, exile this artifact and every creature without a rope counter,
        // then remove all rope counters from the creatures that remain.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new UnlessPaysEffect(Subjects.activePlayer(), new PayManaCost("{2}"),
                SequenceEffect.of(
                        new ExileTargetPermanentEffect(Subjects.allOf(Subjects.source(),
                                Subjects.controlledBy(Subjects.eachPlayer(), new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.ROPE))))))),
                        RemoveCountersEffect.allFrom(Subjects.allCreatures(), CounterType.ROPE)),
                SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1,
                                        Subjects.controlledBy(Subjects.activePlayer(), new PermanentIsCreaturePredicate()),
                                        "Choose a creature to put a rope counter on.")
                                .chosenBy(Subjects.activePlayer()),
                        new PutCountersEffect(Subjects.thisWay(StepKind.CHOOSE), CounterType.ROPE))));
    }
}
