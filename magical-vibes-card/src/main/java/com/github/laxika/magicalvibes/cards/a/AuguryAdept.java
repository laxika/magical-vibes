package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;

@CardRegistration(set = "SHM", collectorNumber = "137")
public class AuguryAdept extends Card {

    public AuguryAdept() {
        // Whenever this creature deals combat damage to a player, reveal the top card of your
        // library and put that card into your hand. You gain life equal to its mana value.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RevealTopCardPutIntoHandAndChangeLifeEffect(true));
    }
}
