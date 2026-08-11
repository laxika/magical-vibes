package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "46")
@CardRegistration(set = "KTK", collectorNumber = "35")
public class CripplingChill extends Card {

    public CripplingChill() {
        // Tap target creature. It doesn't untap during its controller's next untap step.
        // Draw a card.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new SkipNextUntapEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
