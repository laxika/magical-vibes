package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VMA", collectorNumber = "154")
public class ChaosWarp extends Card {

    public ChaosWarp() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, new ShuffleTargetPermanentIntoLibraryEffect(
                        new RevealTopCardPermanentToBattlefieldEffect(),
                        ThenEffectRecipient.TARGET_OWNER));
    }
}
