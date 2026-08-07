package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "ORI", collectorNumber = "127")
public class AbbotOfKeralKeep extends Card {

    public AbbotOfKeralKeep() {
        // When this creature enters, exile the top card of your library. Until end of turn, you may
        // play that card. (Prowess is a Scryfall-loaded keyword.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardMayPlayThisTurnEffect(1, false));
    }
}
