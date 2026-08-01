package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;

/**
 * Bloodfray Giant — {2}{R}{R} Creature — Giant 4/3 with trample and unleash.
 * <p>
 * Trample is a Scryfall-loaded keyword. Unleash is two static abilities: the optional
 * as-enters +1/+1 counter ({@link UnleashEffect}) and "can't block as long as it has a
 * +1/+1 counter on it".
 */
@CardRegistration(set = "RTR", collectorNumber = "89")
public class BloodfrayGiant extends Card {

    public BloodfrayGiant() {
        addEffect(EffectSlot.STATIC, new UnleashEffect());
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                "it has no +1/+1 counters on it"));
    }
}
