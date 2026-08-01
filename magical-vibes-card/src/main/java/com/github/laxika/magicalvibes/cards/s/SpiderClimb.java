package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.FlashCastWithCleanupSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VIS", collectorNumber = "120")
public class SpiderClimb extends Card {

    public SpiderClimb() {
        // Enchant creature; the Mirage flash clause lets it be cast at instant speed at the cost of
        // being sacrificed at the next cleanup step.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new FlashCastWithCleanupSacrificeEffect())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 3, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.REACH, GrantScope.ENCHANTED_CREATURE));
    }
}
