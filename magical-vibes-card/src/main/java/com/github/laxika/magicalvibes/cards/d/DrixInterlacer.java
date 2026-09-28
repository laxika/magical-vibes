package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "29")
public class DrixInterlacer extends Card {

    public DrixInterlacer() {
        // Whenever another artifact you control enters, this artifact intensifies by 1.
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.INTENSITY));

        // {T}, Sacrifice this artifact: Draw X cards, where X is half this artifact's intensity,
        // rounded down. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new DrawCardEffect(new Divided(new CountersOnSource(CounterType.INTENSITY), 2))
                ),
                "{T}, Sacrifice this artifact: Draw X cards, where X is half this artifact's intensity, "
                        + "rounded down. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
