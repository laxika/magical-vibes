package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "32")
public class SinewDancer extends Card {

    public SinewDancer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{3}{W}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "Corrupted — {W}, {T}: Tap target creature. Activate only if an opponent has three or more poison counters.",
                TargetFilters.creature()
        ).withActivationCondition(
                new OpponentPoisoned(3),
                "Activate only if an opponent has three or more poison counters."
        ));
    }
}
