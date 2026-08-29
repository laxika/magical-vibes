package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M10", collectorNumber = "150")
@CardRegistration(set = "9ED", collectorNumber = "207")
@CardRegistration(set = "8ED", collectorNumber = "209")
@CardRegistration(set = "PCY", collectorNumber = "98")
public class PanicAttack extends Card {

    public PanicAttack() {
        target(TargetFilters.creature(), 0, 3)
                .addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
