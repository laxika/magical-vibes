package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "56")
public class DragonTurtle extends Card {

    public DragonTurtle() {
        // Flash is loaded from Scryfall.

        // When this creature enters, tap it and up to one target creature an opponent controls.
        // They don't untap during their controllers' next untap steps.
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.SELF))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SkipNextUntapEffect(TapUntapScope.SELF))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
