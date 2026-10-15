package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentAnyColorManaEffect;

@CardRegistration(set = "MSC", collectorNumber = "58")
@CardRegistration(set = "MSC", collectorNumber = "369")
public class PhotonMightyMarvel extends Card {

    public PhotonMightyMarvel() {
        // Whenever Photon deals combat damage to a player, add that much mana of any one color.
        // Until end of turn, you don't lose this mana as steps and phases end.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new AwardPersistentAnyColorManaEffect(new EventValue()));
    }
}
