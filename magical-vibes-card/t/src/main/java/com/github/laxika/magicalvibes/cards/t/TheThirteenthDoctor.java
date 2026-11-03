package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "4")
@CardRegistration(set = "WHO", collectorNumber = "448")
@CardRegistration(set = "WHO", collectorNumber = "195")
@CardRegistration(set = "WHO", collectorNumber = "564")
@CardRegistration(set = "WHO", collectorNumber = "609")
@CardRegistration(set = "WHO", collectorNumber = "1039")
@CardRegistration(set = "WHO", collectorNumber = "1155")
public class TheThirteenthDoctor extends Card {

    public TheThirteenthDoctor() {
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell().notCastFrom(Zone.HAND),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasCountersPredicate(CounterType.ANY)))));
    }
}
