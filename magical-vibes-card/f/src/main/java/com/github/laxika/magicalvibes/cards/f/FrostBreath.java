package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "54")
@CardRegistration(set = "M14", collectorNumber = "56")
@CardRegistration(set = "M21", collectorNumber = "51")
public class FrostBreath extends Card {

    public FrostBreath() {
        // Tap up to two target creatures. Those creatures don't untap during
        // their controller's next untap step.
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
