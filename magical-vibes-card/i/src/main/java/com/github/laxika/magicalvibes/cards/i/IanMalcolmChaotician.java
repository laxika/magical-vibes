package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "REX", collectorNumber = "13")
@CardRegistration(set = "REX", collectorNumber = "38")
public class IanMalcolmChaotician extends Card {

    public IanMalcolmChaotician() {
        // Whenever a player draws their second card each turn, that player exiles the top card of
        // their library, tracked with Ian Malcolm.
        addTrigger(Triggers.youDrawCard(),
                new NthCardDrawTriggerEffect(2,
                        new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.CONTROLLER)));
        addTrigger(Triggers.opponentDrawsCard(),
                new NthCardDrawTriggerEffect(2,
                        new ExileTopCardsToSourceEffect(
                                1, false, false, LibraryScope.TARGET_OPPONENT)));

        // During each player's turn, that player may cast a non-owned spell exiled with Ian Malcolm;
        // mana of any type can be spent to cast it.
        addStaticEffect(AllowCastFromCardsExiledWithSourceEffect.activePlayerNonOwnedWithAnyMana());
    }
}
