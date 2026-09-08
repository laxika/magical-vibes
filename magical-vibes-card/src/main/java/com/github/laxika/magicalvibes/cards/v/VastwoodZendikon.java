package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "117")
public class VastwoodZendikon extends Card {

    public VastwoodZendikon() {
        // Enchant land. Enchanted land is a 6/4 green Elemental creature. It's still a land.
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesCreatureEffect(
                6, 4, CardColor.GREEN, List.of(CardSubtype.ELEMENTAL)));

        // When enchanted land dies, return that card to its owner's hand.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new ReturnEnchantedCreatureToOwnerHandOnDeathEffect());
    }
}
