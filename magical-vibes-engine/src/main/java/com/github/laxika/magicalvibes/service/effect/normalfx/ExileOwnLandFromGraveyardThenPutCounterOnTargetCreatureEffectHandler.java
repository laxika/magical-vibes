package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnLandFromGraveyardThenPutCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Forgotten Harvest's contingent graveyard exile and its follow-up creature target.
 */
@Component
@RequiredArgsConstructor
public class ExileOwnLandFromGraveyardThenPutCounterOnTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnLandFromGraveyardThenPutCounterOnTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var state = gameData.graveyardTargetOperation;

        boolean choiceMade = state.resolutionTimeExileThenPutCounterOnTargetCreatureChoiceMade;
        UUID chosenCardId = null;
        if (choiceMade) {
            state.resolutionTimeExileThenPutCounterOnTargetCreatureChoiceMade = false;
            chosenCardId = state.resolutionTimeExileThenPutCounterOnTargetCreatureChosenCardId;
            state.resolutionTimeExileThenPutCounterOnTargetCreatureChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
        }
        if (choiceMade && chosenCardId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        Card land = chosenCardId == null
                ? findOnlyMatchingLand(gameData, entry)
                : findMatchingLand(gameData, entry, chosenCardId);
        if (land == null) {
            if (chosenCardId == null) {
                List<Card> candidates = matchingLands(gameData, entry);
                if (candidates.size() > 1) {
                    state.resolutionTimeExileThenPutCounterOnTargetCreatureResume = true;
                    gameData.rerunCurrentEffectAfterInteraction = true;
                    playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(),
                            new ArrayList<>(candidates), 1,
                            entry.getCard().getName() + " — You may exile a land card from your graveyard.");
                }
            }
            return;
        }

        graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, land.getId(), land);
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getCard().getName() + " exiles ", land, " from its controller's graveyard."));

        putCounterOnTarget(gameData, entry, target);
    }

    private List<Card> matchingLands(GameData gameData, StackEntry entry) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return List.of();
        }
        return graveyard.stream()
                .filter(card -> card != entry.getCard())
                .filter(card -> card.hasType(CardType.LAND))
                .toList();
    }

    private Card findOnlyMatchingLand(GameData gameData, StackEntry entry) {
        List<Card> candidates = matchingLands(gameData, entry);
        return candidates.size() == 1 ? candidates.getFirst() : null;
    }

    private Card findMatchingLand(GameData gameData, StackEntry entry, UUID cardId) {
        return matchingLands(gameData, entry).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private void putCounterOnTarget(GameData gameData, StackEntry entry, Permanent target) {
        if (!gameQueryService.isCreature(gameData, target)
                || gameQueryService.cantHaveCounters(gameData, target)
                || gameQueryService.cantHavePlusOnePlusOneCounters(gameData, target)) {
            return;
        }
        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, target,
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 1);
    }
}
