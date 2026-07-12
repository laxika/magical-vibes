package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "SHM", collectorNumber = "7")
public class GreaterAuramancy extends Card {

    public GreaterAuramancy() {
        // Other enchantments you control have shroud. The source permanent is excluded from
        // static bonus computation, so OWN_PERMANENTS models the "other" wording.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.OWN_PERMANENTS,
                new PermanentIsEnchantmentPredicate()));

        // Enchanted creatures you control have shroud.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.OWN_CREATURES,
                new PermanentIsEnchantedPredicate()));
    }
}
