package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.effect.PlayedCardExiledWithSourceTriggerEffect;

@CardRegistration(set = "AFC", collectorNumber = "34")
public class ShareTheSpoils extends Card {

    public ShareTheSpoils() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.EACH_PLAYER));
        addEffect(EffectSlot.ON_PLAYER_LOSES_GAME,
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.EACH_PLAYER));
        addEffect(EffectSlot.STATIC,
                AllowCastFromCardsExiledWithSourceEffect.activePlayerOneCardPerTurnWithAnyMana());

        PlayedCardExiledWithSourceTriggerEffect exileTopCard = new PlayedCardExiledWithSourceTriggerEffect(
                new ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TARGET_PLAYER));
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, exileTopCard);
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, exileTopCard);
        addEffect(EffectSlot.ON_OPPONENT_PLAYS_LAND, exileTopCard);
    }
}
