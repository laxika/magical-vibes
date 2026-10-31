package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "19")
public class ElandUmbra extends Card {

    public ElandUmbra() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(0, 4, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new TotemArmorEffect());
    }
}
