package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "10")
public class JamuraanLion extends Card {

    public JamuraanLion() {
        // {W}, {T}: Target creature can't block this turn.
        addActivatedAbility(new ActivatedAbility(true, "{W}",
                List.of(new CantBlockThisTurnEffect(TapUntapScope.TARGET)),
                "{W}, {T}: Target creature can't block this turn.",
                TargetFilters.creature()));
    }
}
