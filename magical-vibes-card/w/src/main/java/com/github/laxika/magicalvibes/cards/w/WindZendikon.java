package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "46")
public class WindZendikon extends Card {

    public WindZendikon() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesCreatureEffect(
                        2, 2, CardColor.BLUE, List.of(CardSubtype.ELEMENTAL)))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                        Keyword.FLYING, GrantScope.ENCHANTED_PERMANENT));

        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new ReturnEnchantedCreatureToOwnerHandOnDeathEffect());
    }
}
