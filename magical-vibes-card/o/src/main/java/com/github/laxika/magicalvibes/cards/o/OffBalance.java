package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEM", collectorNumber = "15")
public class OffBalance extends Card {

    public OffBalance() {
        target(TargetFilters.creature())
                .addSpellEffect(new CantAttackThisTurnEffect(TapUntapScope.TARGET))
                .addSpellEffect(new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
