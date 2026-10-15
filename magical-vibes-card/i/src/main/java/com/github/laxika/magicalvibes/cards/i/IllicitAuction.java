package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IllicitAuctionEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "190")
@CardRegistration(set = "MIR", collectorNumber = "183")
public class IllicitAuction extends Card {

    public IllicitAuction() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new IllicitAuctionEffect());
    }
}
