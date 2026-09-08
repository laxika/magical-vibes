package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Cemetery cards' non-targeting graveyard choice. */
@Component
@RequiredArgsConstructor
public class ExileAnyGraveyardCardAndImprintOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final ExileService exileService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAnyGraveyardCardAndImprintOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChoiceMade) {
            UUID chosenCardId = gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChosenCardId;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChoiceMade = false;
            gameData.graveyardTargetOperation.resolutionTimeExileThenEffectChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            Card chosen = findCardInAnyGraveyard(gameData, chosenCardId);
            if (chosen == null || !graveyardReturnSupport.exileCardFromAnyGraveyard(
                    gameData, chosenCardId, chosen, entry.getSourcePermanentId())) {
                return;
            }

            exileService.setImprintedCardOnPermanent(gameData, entry.getSourcePermanentId(), chosen);
            gameLogService.append(gameData,
                    GameLog.textCardText(entry.getCard().getName() + " exiles ", chosen,
                            " from a graveyard."));
            return;
        }

        List<Card> candidates = allGraveyardCards(gameData);
        if (candidates.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no card in any graveyard to exile."));
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeExileThenEffectResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(), candidates, 1, 1,
                entry.getCard().getName() + " — Choose a card from a graveyard to exile.");
    }

    private List<Card> allGraveyardCards(GameData gameData) {
        List<Card> cards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null) {
                cards.addAll(graveyard);
            }
        }
        return cards;
    }

    private Card findCardInAnyGraveyard(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            for (Card card : graveyard) {
                if (card.getId().equals(cardId)) {
                    return card;
                }
            }
        }
        return null;
    }
}
