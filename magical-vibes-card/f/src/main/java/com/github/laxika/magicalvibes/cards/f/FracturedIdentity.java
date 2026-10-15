package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndCreateTokenCopiesForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndCreateTokenCopyForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "272")
@CardRegistration(set = "SLD", collectorNumber = "833")
@CardRegistration(set = "OTP", collectorNumber = "45")
@CardRegistration(set = "OTP", collectorNumber = "76")
@CardRegistration(set = "WHO", collectorNumber = "236")
@CardRegistration(set = "WHO", collectorNumber = "477")
@CardRegistration(set = "WHO", collectorNumber = "827")
@CardRegistration(set = "WHO", collectorNumber = "1068")
@CardRegistration(set = "C17", collectorNumber = "37")
public class FracturedIdentity extends Card {

    public FracturedIdentity() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentAndCreateTokenCopyForEachOtherPlayerEffect());
    }
}
