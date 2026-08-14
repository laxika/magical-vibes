package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "95")
public class SowerOfChaos extends Card {

    public SowerOfChaos() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{2}{R}: Target creature can't block this turn.",
                TargetFilters.creature()));
    }
}
