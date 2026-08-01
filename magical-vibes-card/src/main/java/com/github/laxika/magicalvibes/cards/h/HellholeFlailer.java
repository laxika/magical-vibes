package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;

import java.util.List;

/**
 * Hellhole Flailer — {1}{B}{R} Creature — Ogre Warrior 3/2 with unleash.
 * <p>
 * Unleash is two static abilities: the optional as-enters +1/+1 counter
 * ({@link UnleashEffect}) and "can't block as long as it has a +1/+1 counter on it".
 * <p>
 * {2}{B}{R}, Sacrifice this creature: It deals damage equal to its power to target
 * player or planeswalker. Power is read from the last-known snapshot after the
 * sacrifice cost (same pattern as Flame Elemental).
 */
@CardRegistration(set = "RTR", collectorNumber = "167")
public class HellholeFlailer extends Card {

    public HellholeFlailer() {
        addEffect(EffectSlot.STATIC, new UnleashEffect());
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new NotCondition(new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                "it has no +1/+1 counters on it"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{R}",
                List.of(
                        new SacrificeSelfCost(),
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(new SourcePower())
                ),
                "{2}{B}{R}, Sacrifice this creature: It deals damage equal to its power to "
                        + "target player or planeswalker."
        ));
    }
}
