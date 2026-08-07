package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;

@CardRegistration(set = "GTC", collectorNumber = "222")
public class NightveilSpecter extends Card {

    public NightveilSpecter() {
        // Whenever this creature deals combat damage to a player, that player exiles the top card
        // of their library (face up, tracked with this creature).
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_OPPONENT));

        // You may play lands and cast spells from among cards exiled with this creature.
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(false));
    }
}
