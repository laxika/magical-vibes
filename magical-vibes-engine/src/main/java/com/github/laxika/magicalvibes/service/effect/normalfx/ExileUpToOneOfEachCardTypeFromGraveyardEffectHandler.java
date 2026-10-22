package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneOfEachCardTypeFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileUpToOneOfEachCardTypeFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUpToOneOfEachCardTypeFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var state = gameData.graveyardTargetOperation;
        UUID defendingPlayerId = defendingPlayerId(gameData, entry);

        if (!state.resolutionTimeExileOneOfEachCardTypeChoiceMade) {
            List<Card> graveyard = defendingPlayerId == null
                    ? null : gameData.playerGraveyards.get(defendingPlayerId);
            if (graveyard == null || graveyard.isEmpty()) {
                return;
            }
            state.resolutionTimeExileOneOfEachCardTypeResume = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(),
                    new ArrayList<>(graveyard), ((ExileUpToOneOfEachCardTypeFromGraveyardEffect) effect)
                            .cardTypeSlots().size(), 0,
                    entry.getCard().getName() + "'s ability — Choose up to one card of each card type"
                            + " from defending player's graveyard to exile.");
            return;
        }

        List<UUID> targetCardIds = state.resolutionTimeExileOneOfEachCardTypeChosenCardIds;
        state.resolutionTimeExileOneOfEachCardTypeChoiceMade = false;
        state.resolutionTimeExileOneOfEachCardTypeChosenCardIds = null;
        gameData.rerunCurrentEffectAfterInteraction = false;

        List<Card> exiledCards = new ArrayList<>();
        if (targetCardIds != null) {
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card)) {
                    exiledCards.add(card);
                }
            }
        }

        if (!exiledCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    entry.getCard().getName() + " exiles " + exiledCards.size()
                            + " card" + (exiledCards.size() == 1 ? "" : "s") + " from a graveyard."));
            if (entry.getSourcePermanentId() != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (source != null) {
                    permanentCounterSupport.applyPlusOnePlusOneCounters(
                            gameData, entry, source, exiledCards.size());
                }
            }
        }
    }

    private UUID defendingPlayerId(GameData gameData, StackEntry entry) {
        if (entry.getDefendingPlayerId() != null) {
            return entry.getDefendingPlayerId();
        }
        UUID attackedId = entry.getAttackedTargetId();
        if (attackedId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedId)
                ? attackedId : gameQueryService.findPermanentController(gameData, attackedId);
    }
}
