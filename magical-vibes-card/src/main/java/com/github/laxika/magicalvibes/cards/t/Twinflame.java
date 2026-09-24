package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "115")
@CardRegistration(set = "SLD", collectorNumber = "1810")
@CardRegistration(set = "2X2", collectorNumber = "129")
public class Twinflame extends Card {

    public Twinflame() {
        setAdditionalManaCostPerExtraTarget("{2}{R}");
        target(TargetFilters.creatureYouControl(), 0, 99)
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect(true, true));
    }
}
