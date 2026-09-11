package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeAuraAndGrantIndestructibleEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "136")
public class CracklingEmergence extends Card {

    public CracklingEmergence() {
        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesCreatureEffect(
                        3, 3, CardColor.RED, List.of(CardSubtype.SPIRIT)))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC, new SacrificeAuraAndGrantIndestructibleEffect());
    }
}
