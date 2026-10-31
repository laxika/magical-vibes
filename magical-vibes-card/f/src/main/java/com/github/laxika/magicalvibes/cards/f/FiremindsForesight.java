package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForInstantsWithManaValuesToHandEffect;

@CardRegistration(set = "RTR", collectorNumber = "162")
@CardRegistration(set = "DDS", collectorNumber = "21")
@CardRegistration(set = "C15", collectorNumber = "218")
public class FiremindsForesight extends Card {

    public FiremindsForesight() {
        addSpellEffect(new SearchLibraryForInstantsWithManaValuesToHandEffect());
    }
}
