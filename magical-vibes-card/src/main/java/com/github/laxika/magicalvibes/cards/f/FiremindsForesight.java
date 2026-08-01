package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForInstantsWithManaValuesToHandEffect;

@CardRegistration(set = "RTR", collectorNumber = "162")
public class FiremindsForesight extends Card {

    public FiremindsForesight() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForInstantsWithManaValuesToHandEffect());
    }
}
