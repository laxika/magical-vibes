package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnPermanentTypeToHandEffect;

@CardRegistration(set = "RAV", collectorNumber = "44")
public class DrakeFamiliar extends Card {

    public DrakeFamiliar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificeUnlessReturnPermanentTypeToHandEffect(CardType.ENCHANTMENT));
    }
}
