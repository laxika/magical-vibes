package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsSourceCountersPredicate;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "86")
public class WaveOfTerror extends Card {

    public WaveOfTerror() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        // The draw step follows the upkeep, so the age counter added this turn is already on the
        // enchantment when this trigger resolves.
        addEffect(EffectSlot.DRAW_TRIGGERED, new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentManaValueEqualsSourceCountersPredicate(CounterType.AGE)
                )),
                true));
    }
}
