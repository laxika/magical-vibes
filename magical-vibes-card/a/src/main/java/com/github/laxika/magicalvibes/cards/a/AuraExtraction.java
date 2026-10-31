package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "5")
public class AuraExtraction extends Card {

    public AuraExtraction() {
        target(TargetFilters.enchantment())
                .addSpellEffect(new PutTargetOnTopOfLibraryEffect());
        addCycling("{2}");
    }
}
