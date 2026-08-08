package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;

/**
 * Rakdos Drake — {2}{B} Creature — Drake 1/2 with flying and unleash.
 * <p>
 * Flying comes from Scryfall. Unleash is two static abilities: the optional
 * as-enters +1/+1 counter ({@link UnleashEffect}) and "can't block as long as it
 * has a +1/+1 counter on it".
 */
@CardRegistration(set = "DGM", collectorNumber = "28")
public class RakdosDrake extends Card {

    public RakdosDrake() {
        addEffect(EffectSlot.STATIC, new UnleashEffect());
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                "it has no +1/+1 counters on it"));
    }
}
