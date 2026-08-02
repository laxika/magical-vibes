package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "M15", collectorNumber = "126")
public class ActOnImpulse extends Card {

    public ActOnImpulse() {
        // Exile the top three cards of your library. Until end of turn, you may play those cards.
        addEffect(EffectSlot.SPELL, new ExileTopCardMayPlayThisTurnEffect(3, false));
    }
}
