package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "22")
public class GyoxBrutalCarnivora extends Card {

    public GyoxBrutalCarnivora() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.endStep(ActivePlayer.YOU),
                        new PutCounterOnTargetPermanentEffect(CounterType.OIL));

        // X is the number of oil counters on the creature as it last existed on the battlefield (CR 603.10a).
        var oilCounters = new CountersOnTriggeringPermanent(CounterType.OIL);
        addTrigger(Triggers.creatureDies(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                new PermanentHasCountersPredicate(CounterType.OIL))),
                        ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(
                        ConjureCardsEffect.duplicateOf(Subjects.triggering(), ConjureDestination.intoExile())
                                .times(oilCounters),
                        new PerpetuallyBoostCardEffect(null, oilCounters, oilCounters,
                                Subjects.cardsThisWay(StepKind.CONJURE)),
                        // "Shuffle those duplicates into your library" shuffles it even if none is still in exile
                        // (CR 701.24c).
                        PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.cardsThisWay(StepKind.CONJURE)),
                        new ShuffleLibraryEffect(false)));
    }
}
