package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "98")
@CardRegistration(set = "C16", collectorNumber = "115")
@CardRegistration(set = "CM2", collectorNumber = "70")
public class Necroplasm extends Card {

    public Necroplasm() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PutCountersOnSourceEffect(1, 1, 1));
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentManaValueEqualsSourceCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
                ))));
        addStaticEffect(new DredgeEffect(2));
    }
}
