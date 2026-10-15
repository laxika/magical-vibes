package com.github.laxika.magicalvibes.cards.g;

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

@CardRegistration(set = "WWK", collectorNumber = "6")
public class GuardianZendikon extends Card {

    public GuardianZendikon() {
        // Enchant land. Enchanted land is a 2/6 white Wall creature with defender. It's still a land.
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesCreatureEffect(
                        2, 6, CardColor.WHITE, List.of(CardSubtype.WALL)))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                        Keyword.DEFENDER, GrantScope.ENCHANTED_PERMANENT));

        // When enchanted land dies, return that card to its owner's hand.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new ReturnEnchantedCreatureToOwnerHandOnDeathEffect());
    }
}
