package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSacrificedPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a return of the exact permanent sacrificed earlier in the same ability. */
@Component
@RequiredArgsConstructor
public class ReturnSacrificedPermanentToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSacrificedPermanentToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSacrificedPermanentSnapshot() == null
                || entry.getSacrificedPermanentSnapshot().getCard() == null) {
            return;
        }

        UUID cardId = entry.getSacrificedPermanentSnapshot().getCard().getId();
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (card == null || ownerId == null) {
            // CR 400.7j: an effect can find the object it moved to a public zone, so a sacrificed
            // card that a replacement effect exiled instead can still be returned.
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled != null && gameData.removeFromExile(cardId)) {
                ReturnSacrificedPermanentToBattlefieldEffect fromExile =
                        (ReturnSacrificedPermanentToBattlefieldEffect) effect;
                graveyardReturnSupport.putCardOntoBattlefieldFromExileWithCounters(
                        gameData, exiled.ownerId(), exiled.card(), fromExile.counterType(), fromExile.counterAmount());
            }
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        ReturnSacrificedPermanentToBattlefieldEffect returnEffect =
                (ReturnSacrificedPermanentToBattlefieldEffect) effect;
        graveyardReturnSupport.putCardOntoBattlefield(
                gameData, ownerId, card, null, null, false, false,
                returnEffect.counterType(), returnEffect.counterAmount(), false);
    }
}
