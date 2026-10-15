package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithGameResetEffect;

@CardRegistration(set = "ALA", collectorNumber = "210")
public class LichsMirror extends Card {

    public LichsMirror() {
        // If you would lose the game, instead shuffle your hand, your graveyard, and all permanents
        // you own into your library, then draw seven cards and your life total becomes 20.
        addEffect(EffectSlot.STATIC, new ReplaceControllerLossWithGameResetEffect());
    }
}
