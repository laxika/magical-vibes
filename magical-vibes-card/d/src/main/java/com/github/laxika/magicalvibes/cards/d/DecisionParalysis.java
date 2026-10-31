package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AKH", collectorNumber = "50")
@CardRegistration(set = "GN2", collectorNumber = "19")
public class DecisionParalysis extends Card {

    public DecisionParalysis() {
        // Tap up to two target creatures. Those creatures don't untap during
        // their controller's next untap step.
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
