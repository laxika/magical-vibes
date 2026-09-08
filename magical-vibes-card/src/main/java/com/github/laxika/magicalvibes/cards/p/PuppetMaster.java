package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandThenMayPayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHR", collectorNumber = "23")
public class PuppetMaster extends Card {

    public PuppetMaster() {
        // Enchant creature
        target(TargetFilters.creature());

        // When enchanted creature dies, return that card to its owner's hand. If that card is
        // returned this way, you may pay {U}{U}{U}. If you do, return this card to its owner's hand.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new ReturnEnchantedCreatureToOwnerHandThenMayPayEffect("{U}{U}{U}"));
    }
}
