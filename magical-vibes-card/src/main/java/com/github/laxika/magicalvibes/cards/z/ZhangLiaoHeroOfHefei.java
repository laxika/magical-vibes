package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "PTK", collectorNumber = "96")
public class ZhangLiaoHeroOfHefei extends Card {

    public ZhangLiaoHeroOfHefei() {
        // Whenever this creature deals damage to an opponent, that player (the damaged opponent)
        // discards a card. The engine routes TARGET_PLAYER discards on this slot to the damaged player.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false));
    }
}
