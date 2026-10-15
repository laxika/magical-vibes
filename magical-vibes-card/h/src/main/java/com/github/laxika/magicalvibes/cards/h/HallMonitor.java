package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "105")
public class HallMonitor extends Card {

    public HallMonitor() {
        addActivatedAbility(new ActivatedAbility(true, "{1}{R}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{1}{R}, {T}: Target creature can't block this turn.",
                TargetFilters.creature()));
    }
}
