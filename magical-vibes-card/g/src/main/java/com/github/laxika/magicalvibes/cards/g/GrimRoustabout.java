package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;

import java.util.List;

/**
 * Grim Roustabout — {1}{B} Creature — Skeleton Warrior 1/1 with unleash and a regeneration ability.
 * <p>
 * Unleash is two static abilities (CR 702.98a): the optional as-enters +1/+1 counter
 * ({@link UnleashEffect}) and "can't block as long as it has a +1/+1 counter on it".
 */
@CardRegistration(set = "RTR", collectorNumber = "68")
public class GrimRoustabout extends Card {

    public GrimRoustabout() {
        addEffect(EffectSlot.STATIC, new UnleashEffect());
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                "it has no +1/+1 counters on it"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Grim Roustabout."
        ));
    }
}
