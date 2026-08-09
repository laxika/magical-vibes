package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "UDS", collectorNumber = "29")
public class BubblingBeebles extends Card {

    public BubblingBeebles() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(
                new PermanentIsEnchantmentPredicate()
        ));
    }
}
