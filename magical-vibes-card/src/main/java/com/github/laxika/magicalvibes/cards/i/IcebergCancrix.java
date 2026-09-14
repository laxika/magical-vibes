package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "HA4", collectorNumber = "5")
public class IcebergCancrix extends Card {

    public IcebergCancrix() {
        // Whenever another snow permanent you control enters, you may have target player mill two cards.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSupertypePredicate(CardSupertype.SNOW),
                        new MayEffect(new MillEffect(2, MillRecipient.TARGET_PLAYER), "Mill two cards?")));
    }
}
