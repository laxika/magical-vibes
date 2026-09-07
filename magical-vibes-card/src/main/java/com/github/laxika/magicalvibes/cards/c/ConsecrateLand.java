package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeEnchantedByOtherAurasEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSB", collectorNumber = "4")
public class ConsecrateLand extends Card {

    public ConsecrateLand() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC,
                        new GrantEffectEffect(new CantBeEnchantedByOtherAurasEffect(),
                                GrantScope.ENCHANTED_PERMANENT));
    }
}
