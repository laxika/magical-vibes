package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RaidingPartyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Raiding Party's two-stage, active-player-order choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RaidingPartyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TapUntapSupport tapUntapSupport;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RaidingPartyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> playerIds = apnapPlayers(gameData);
        stepTap(gameData, playerIds, 0, new ArrayList<>(Collections.nCopies(playerIds.size(), 0)),
                entry.getCard().getName());
    }

    public void completeTapChoice(GameData gameData, List<UUID> chosenIds,
                                  MultiPermanentChoiceContext.RaidingPartyTapChoice context) {
        int tappedCount = 0;
        for (UUID permanentId : chosenIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && tapUntapSupport.tapPermanent(gameData, permanent)) {
                tappedCount++;
            }
        }

        List<Integer> tappedCounts = new ArrayList<>(context.tappedCounts());
        tappedCounts.set(context.playerIndex(), tappedCount);
        stepTap(gameData, context.playerIds(), context.playerIndex() + 1, tappedCounts,
                context.sourceName());
    }

    public void completePlainsChoice(GameData gameData, List<UUID> chosenIds,
                                     MultiPermanentChoiceContext.RaidingPartyPlainsChoice context) {
        List<UUID> chosenPlains = new ArrayList<>(context.chosenPlains());
        chosenPlains.addAll(chosenIds);
        stepPlains(gameData, context.playerIds(), context.playerIndex() + 1,
                context.tappedCounts(), chosenPlains, context.sourceName());
    }

    private void stepTap(GameData gameData, List<UUID> playerIds, int playerIndex,
                         List<Integer> tappedCounts, String sourceName) {
        for (int currentPlayerIndex = playerIndex; currentPlayerIndex < playerIds.size(); currentPlayerIndex++) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            List<UUID> candidates = untappedWhiteCreatureIds(gameData, playerId);
            if (candidates.isEmpty()) {
                tappedCounts.set(currentPlayerIndex, 0);
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, playerId, candidates, candidates.size(),
                    new MultiPermanentChoiceContext.RaidingPartyTapChoice(
                            playerIds, currentPlayerIndex, tappedCounts, sourceName),
                    sourceName + " — choose any number of untapped white creatures to tap.");
            return;
        }

        stepPlains(gameData, playerIds, 0, tappedCounts, List.of(), sourceName);
    }

    private void stepPlains(GameData gameData, List<UUID> playerIds, int playerIndex,
                            List<Integer> tappedCounts, List<UUID> chosenPlains, String sourceName) {
        for (int currentPlayerIndex = playerIndex; currentPlayerIndex < playerIds.size(); currentPlayerIndex++) {
            int maxCount = tappedCounts.get(currentPlayerIndex) * 2;
            if (maxCount == 0) {
                continue;
            }

            List<UUID> candidates = plainsIds(gameData);
            if (candidates.isEmpty()) {
                continue;
            }

            UUID playerId = playerIds.get(currentPlayerIndex);
            playerInputService.beginMultiPermanentChoice(
                    gameData, playerId, candidates, Math.min(maxCount, candidates.size()),
                    new MultiPermanentChoiceContext.RaidingPartyPlainsChoice(
                            playerIds, currentPlayerIndex, tappedCounts, chosenPlains, sourceName),
                    sourceName + " — choose up to " + Math.min(maxCount, candidates.size()) + " Plains.");
            return;
        }

        destroyUnchosenPlains(gameData, chosenPlains, sourceName);
    }

    private void destroyUnchosenPlains(GameData gameData, List<UUID> chosenPlains, String sourceName) {
        Set<UUID> chosenIds = new HashSet<>(chosenPlains);
        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (isPlains(gameData, permanent) && !chosenIds.contains(permanent.getId())) {
                    toDestroy.add(permanent);
                }
            }
        });

        if (toDestroy.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(sourceName + " resolves but destroys no Plains."));
            return;
        }

        destructionSupport.destroyBatchCollecting(gameData, toDestroy, sourceName, false);
        log.info("Game {} - {} destroys {} Plains", gameData.id, sourceName, toDestroy.size());
    }

    private List<UUID> untappedWhiteCreatureIds(GameData gameData, UUID playerId) {
        List<UUID> result = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (!permanent.isTapped()
                    && gameQueryService.isCreature(gameData, permanent)
                    && gameQueryService.hasColor(gameData, permanent, CardColor.WHITE)) {
                result.add(permanent.getId());
            }
        }
        return result;
    }

    private List<UUID> plainsIds(GameData gameData) {
        List<UUID> result = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (isPlains(gameData, permanent)) {
                    result.add(permanent.getId());
                }
            }
        });
        return result;
    }

    private boolean isPlains(GameData gameData, Permanent permanent) {
        return gameQueryService.isLand(gameData, permanent)
                && gameQueryService.cardHasSubtype(permanent.getCard(), CardSubtype.PLAINS,
                gameData, permanent.getCard().getOwnerId());
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
