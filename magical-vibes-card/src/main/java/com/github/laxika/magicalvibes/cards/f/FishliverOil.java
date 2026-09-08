package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "9ED", collectorNumber = "77")
@CardRegistration(set = "CHR", collectorNumber = "20")
public class FishliverOil extends Card {

    public FishliverOil() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.ISLANDWALK, GrantScope.ENCHANTED_CREATURE));
    }
}
