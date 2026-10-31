package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PC2", collectorNumber = "96")
@CardRegistration(set = "PCA", collectorNumber = "96")
public class IndrikUmbra extends Card {

    public IndrikUmbra() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(4, 4, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new MustBeBlockedByAllCreaturesEffect())
                .addStaticEffect(new TotemArmorEffect());
    }
}
