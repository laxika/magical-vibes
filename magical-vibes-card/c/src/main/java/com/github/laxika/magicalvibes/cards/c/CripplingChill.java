package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "46")
@CardRegistration(set = "KTK", collectorNumber = "35")
@CardRegistration(set = "MM3", collectorNumber = "34")
public class CripplingChill extends Card {

    public CripplingChill() {
        // Tap target creature. It doesn't untap during its controller's next untap step.
        // Draw a card.
        target(TargetFilters.creature())
                .addSpellEffect(new TapPermanentsEffect(TapUntapScope.TARGET))
                .addSpellEffect(new SkipNextUntapEffect(TapUntapScope.TARGET))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
