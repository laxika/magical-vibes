package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "WTH", collectorNumber = "77")
public class OdylicWraith extends Card {

    public OdylicWraith() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false));
    }
}
