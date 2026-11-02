package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.PlayedCardExiledWithSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFC", collectorNumber = "34")
public class ShareTheSpoils extends Card {

    public ShareTheSpoils() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.EACH_PLAYER));
        addEffect(EffectSlot.ON_PLAYER_LOSES_GAME,
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.EACH_PLAYER));
        addStaticEffect(AllowCastFromCardsExiledWithSourceEffect.activePlayerOneCardPerTurnWithAnyMana());

        PlayedCardExiledWithSourceTriggerEffect exileTopCard = new PlayedCardExiledWithSourceTriggerEffect(
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_PLAYER));
        addTrigger(Triggers.playerCastsSpell(), exileTopCard);
        addTrigger(Triggers.youPlayLand(), exileTopCard);
        addTrigger(Triggers.opponentPlaysLand(), exileTopCard);
    }
}
