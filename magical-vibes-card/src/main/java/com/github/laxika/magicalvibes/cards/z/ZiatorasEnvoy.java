package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPlayLandOrCastFreeEffect;

@CardRegistration(set = "SNC", collectorNumber = "232")
public class ZiatorasEnvoy extends Card {

    public ZiatorasEnvoy() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LookAtTopCardMayPlayLandOrCastFreeEffect(new EventValue()));
    }
}
