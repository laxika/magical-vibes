package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FDN", collectorNumber = "85")
@CardRegistration(set = "FDN", collectorNumber = "328")
@CardRegistration(set = "FDN", collectorNumber = "390")
@CardRegistration(set = "FDN", collectorNumber = "465")
public class Electroduplicate extends Card {

    public Electroduplicate() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect(true, false, true));
        addCastingOption(new FlashbackCast("{2}{R}{R}"));
    }
}
