package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;

/**
 * Spawn of Rix Maadi — {3}{B}{R} Creature — Horror 5/3 with unleash.
 * <p>
 * Unleash is two static abilities: the optional as-enters +1/+1 counter
 * ({@link UnleashEffect}) and "can't block as long as it has a +1/+1 counter on it".
 */
@CardRegistration(set = "RTR", collectorNumber = "199")
public class SpawnOfRixMaadi extends Card {

    public SpawnOfRixMaadi() {
        addEffect(EffectSlot.STATIC, new UnleashEffect());
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                "it has no +1/+1 counters on it"));
    }
}
