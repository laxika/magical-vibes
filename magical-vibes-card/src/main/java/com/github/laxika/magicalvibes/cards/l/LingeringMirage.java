package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "USG", collectorNumber = "84")
public class LingeringMirage extends Card {

    public LingeringMirage() {
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC,
                new EnchantedPermanentBecomesTypeEffect(CardSubtype.ISLAND));
        addCycling("{2}");
    }
}
