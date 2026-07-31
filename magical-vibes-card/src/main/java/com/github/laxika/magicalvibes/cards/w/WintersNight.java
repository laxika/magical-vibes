package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddProducedManaWhenSnowLandTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TappedSnowLandDoesntUntapEffect;

@CardRegistration(set = "ALL", collectorNumber = "114")
public class WintersNight extends Card {

    public WintersNight() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new AddProducedManaWhenSnowLandTappedEffect());
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND, new TappedSnowLandDoesntUntapEffect());
    }
}
