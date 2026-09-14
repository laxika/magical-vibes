package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndCreateTokenCopyForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTP", collectorNumber = "45")
@CardRegistration(set = "OTP", collectorNumber = "76")
public class FracturedIdentity extends Card {

    public FracturedIdentity() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentAndCreateTokenCopyForEachOtherPlayerEffect());
    }
}
