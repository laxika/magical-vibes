package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;

@CardRegistration(set = "REX", collectorNumber = "13")
@CardRegistration(set = "REX", collectorNumber = "38")
public class IanMalcolmChaotician extends Card {

    public IanMalcolmChaotician() {
        // Whenever a player draws their second card each turn, that player exiles the top card of
        // their library, tracked with Ian Malcolm.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new NthCardDrawTriggerEffect(2,
                        new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.CONTROLLER)));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new NthCardDrawTriggerEffect(2,
                        new ExileTopCardsToSourceEffect(
                                1, false, false, LibraryScope.TARGET_OPPONENT)));

        // During each player's turn, that player may cast a non-owned spell exiled with Ian Malcolm;
        // mana of any type can be spent to cast it.
        addEffect(EffectSlot.STATIC,
                AllowCastFromCardsExiledWithSourceEffect.activePlayerNonOwnedWithAnyMana());
    }
}
