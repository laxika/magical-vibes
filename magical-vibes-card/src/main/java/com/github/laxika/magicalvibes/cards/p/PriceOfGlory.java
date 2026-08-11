package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;

@CardRegistration(set = "ODY", collectorNumber = "214")
public class PriceOfGlory extends Card {

    public PriceOfGlory() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
    }
}
