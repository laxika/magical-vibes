package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "157")
public class SiegebreakerGiant extends Card {

    public SiegebreakerGiant() {
        // {3}{R}: Target creature can't block this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{3}{R}: Target creature can't block this turn.",
                TargetFilters.creature()
        ));
    }
}
