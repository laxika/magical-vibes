package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraEnchantCreaturePredicate;

@CardRegistration(set = "TMP", collectorNumber = "84")
public class RootwaterShaman extends Card {

    public RootwaterShaman() {
        // You may cast Aura spells with enchant creature as though they had flash.
        addEffect(EffectSlot.STATIC,
                new GrantFlashToCardTypeEffect(new CardIsAuraEnchantCreaturePredicate()));
    }
}
