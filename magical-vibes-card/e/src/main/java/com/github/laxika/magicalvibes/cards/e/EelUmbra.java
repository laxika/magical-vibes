package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "65")
@CardRegistration(set = "UMA", collectorNumber = "53")
@CardRegistration(set = "2X2", collectorNumber = "48")
@CardRegistration(set = "AFC", collectorNumber = "83")
@CardRegistration(set = "C18", collectorNumber = "89")
public class EelUmbra extends Card {

    public EelUmbra() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new TotemArmorEffect());
    }
}
