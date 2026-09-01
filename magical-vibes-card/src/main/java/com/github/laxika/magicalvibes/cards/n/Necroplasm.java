package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsSourceCountersPredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "98")
public class Necroplasm extends Card {

    public Necroplasm() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSourceEffect(1, 1, 1));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentManaValueEqualsSourceCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)
                ))));
        addEffect(EffectSlot.GRAVEYARD_DRAW_REPLACEMENT, new DredgeEffect(2));
    }
}
