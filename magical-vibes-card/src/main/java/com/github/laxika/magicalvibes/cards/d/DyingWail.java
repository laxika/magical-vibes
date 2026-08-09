package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "58")
public class DyingWail extends Card {

    public DyingWail() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
    }
}
