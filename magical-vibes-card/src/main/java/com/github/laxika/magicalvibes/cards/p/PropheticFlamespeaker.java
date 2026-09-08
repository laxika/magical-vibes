package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "JOU", collectorNumber = "106")
public class PropheticFlamespeaker extends Card {

    public PropheticFlamespeaker() {
        // Whenever this creature deals combat damage to a player, exile the top card of your library.
        // Until end of turn, you may play that card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
