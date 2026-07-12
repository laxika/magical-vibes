package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ThievesAuctionEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "227")
public class ThievesAuction extends Card {

    public ThievesAuction() {
        addEffect(EffectSlot.SPELL, new ThievesAuctionEffect());
    }
}
