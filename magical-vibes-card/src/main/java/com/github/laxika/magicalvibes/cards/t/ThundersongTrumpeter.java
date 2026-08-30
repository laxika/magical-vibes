package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "235")
public class ThundersongTrumpeter extends Card {

    public ThundersongTrumpeter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new CantAttackThisTurnEffect(TapUntapScope.TARGET),
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET)
                ),
                "{T}: Target creature can't attack or block this turn.",
                TargetFilters.creature()
        ));
    }
}
