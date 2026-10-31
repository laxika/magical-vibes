package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "26")
@CardRegistration(set = "PC2", collectorNumber = "8")
@CardRegistration(set = "UMA", collectorNumber = "21")
@CardRegistration(set = "PCA", collectorNumber = "8")
@CardRegistration(set = "2X2", collectorNumber = "13")
public class HyenaUmbra extends Card {

    public HyenaUmbra() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new TotemArmorEffect());
    }
}
