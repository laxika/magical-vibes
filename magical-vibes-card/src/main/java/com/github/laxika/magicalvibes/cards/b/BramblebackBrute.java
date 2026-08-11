package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "128")
public class BramblebackBrute extends Card {

    public BramblebackBrute() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCountersOnSourceEffect(-1, -1, 2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(
                        new RemoveCounterFromSourceCost(),
                        new MakeCreatureUnblockableEffect()
                ),
                "{1}{R}, Remove a counter from this creature: Target creature can't block this turn. Activate only as a sorcery.",
                TargetFilters.creature(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
