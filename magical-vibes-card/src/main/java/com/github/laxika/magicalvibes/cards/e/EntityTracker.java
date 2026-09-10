package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DSK", collectorNumber = "53")
public class EntityTracker extends Card {

    public EntityTracker() {
        DrawCardEffect drawCard = new DrawCardEffect(1);
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, drawCard);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, drawCard);
    }
}
