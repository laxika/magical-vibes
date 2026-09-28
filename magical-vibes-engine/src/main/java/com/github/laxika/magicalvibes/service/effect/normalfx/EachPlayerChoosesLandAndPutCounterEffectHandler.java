package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandAndPutCounterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the effect controller's per-player land target choices for a counter-marking effect. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesLandAndPutCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesLandAndPutCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerChoosesLandAndPutCounterEffect counterEffect =
                (EachPlayerChoosesLandAndPutCounterEffect) effect;
        step(gameData, apnapPlayers(gameData), 0, List.of(), counterEffect.counterType(),
                entry.getCard().getName(), entry.getControllerId());
    }

    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesLandAndPutCounterChoice context) {
        List<UUID> allChosenIds = new ArrayList<>(context.chosenIds());
        allChosenIds.addAll(chosenIds);
        step(gameData, context.playerIds(), context.playerIndex() + 1, allChosenIds,
                context.counterType(), context.sourceName(), context.chooserId());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex,
                      List<UUID> chosenIds, CounterType counterType, String sourceName,
                      UUID chooserId) {
        List<UUID> allChosenIds = new ArrayList<>(chosenIds);

        for (int currentPlayerIndex = playerIndex; currentPlayerIndex < playerIds.size(); currentPlayerIndex++) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            List<UUID> candidates = landIds(gameData, playerId);

            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                allChosenIds.add(candidates.getFirst());
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, chooserId, candidates, 1,
                    new MultiPermanentChoiceContext.EachPlayerChoosesLandAndPutCounterChoice(
                            playerIds, currentPlayerIndex, allChosenIds, counterType, sourceName, chooserId),
                    sourceName + " — choose a land to put a "
                            + counterType.name().toLowerCase() + " counter on.");
            return;
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry != null) {
            for (UUID permanentId : allChosenIds) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
                if (permanent != null) {
                    permanentCounterSupport.placeCounterOnPermanent(gameData, entry, permanent, counterType, 1);
                }
            }
        }
    }

    private List<UUID> landIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isLand(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
