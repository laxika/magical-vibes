package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFC", collectorNumber = "34")
public class ShareTheSpoils extends Card {

    public ShareTheSpoils() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.EACH_PLAYER));
        addTrigger(Triggers.playerLosesGame(),
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.EACH_PLAYER));
        addStaticEffect(AllowCastFromCardsExiledWithSourceEffect.activePlayerOneCardPerTurnWithAnyMana());

        // When they do, exile the top card of their library.
        ExileTopCardsToSourceEffect exileTopCard =
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TRIGGERING_PLAYER);
        addTrigger(Triggers.playerCastsSpell().exiledWithThis(), exileTopCard);
        addTrigger(Triggers.youPlayLand().exiledWithThis(), exileTopCard);
        addTrigger(Triggers.opponentPlaysLand().exiledWithThis(), exileTopCard);
    }
}
