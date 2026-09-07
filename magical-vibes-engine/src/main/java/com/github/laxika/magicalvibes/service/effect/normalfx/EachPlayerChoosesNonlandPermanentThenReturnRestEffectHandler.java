package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesNonlandPermanentThenReturnRestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves the choices and mass return of {@link EachPlayerChoosesNonlandPermanentThenReturnRestEffect}. */
@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerChoosesNonlandPermanentThenReturnRestEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesNonlandPermanentThenReturnRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        step(gameData, apnapPlayers(gameData), 0, List.of(), entry.getCard().getName());
    }

    /** Continues with the next player after the current player chooses a permanent to keep. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesNonlandPermanentThenReturnRestChoice context) {
        List<UUID> keptIds = new ArrayList<>(context.keptIds());
        keptIds.addAll(chosenIds);
        step(gameData, context.playerIds(), context.playerIndex() + 1, keptIds, context.sourceName());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex,
                      List<UUID> keptIds, String sourceName) {
        List<UUID> allKeptIds = new ArrayList<>(keptIds);

        for (int currentPlayerIndex = playerIndex; currentPlayerIndex < playerIds.size(); currentPlayerIndex++) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            List<UUID> candidates = nonlandPermanentIds(gameData, playerId);

            if (candidates.isEmpty()) {
                continue;
            }
            if (candidates.size() == 1) {
                allKeptIds.add(candidates.getFirst());
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, playerId, candidates, 1,
                    new MultiPermanentChoiceContext.EachPlayerChoosesNonlandPermanentThenReturnRestChoice(
                            playerIds, currentPlayerIndex, allKeptIds, sourceName),
                    sourceName + " — choose a nonland permanent to keep.");
            return;
        }

        returnRest(gameData, new HashSet<>(allKeptIds), sourceName);
    }

    private List<UUID> nonlandPermanentIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> !gameQueryService.isLand(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private void returnRest(GameData gameData, Set<UUID> keptIds, String sourceName) {
        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> toReturn.addAll(battlefield.stream()
                .filter(permanent -> !keptIds.contains(permanent.getId()))
                .filter(permanent -> !gameQueryService.isLand(gameData, permanent))
                .toList()));

        for (Permanent permanent : toReturn) {
            Card card = permanent.getCard();
            if (permanentRemovalService.removePermanentToHand(gameData, permanent)) {
                gameLogService.append(gameData, GameLog.cardThen(card, " is returned to its owner's hand."));
                log.info("Game {} - {} returns {} to its owner's hand", gameData.id, sourceName,
                        card.getName());
            }
        }

        if (!toReturn.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
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
