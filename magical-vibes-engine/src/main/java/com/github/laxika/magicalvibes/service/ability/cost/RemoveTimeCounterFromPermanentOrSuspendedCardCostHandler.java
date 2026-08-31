package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RemoveTimeCounterFromExiledCardEffectHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Pays the mixed permanent-or-suspended-card time-counter cost. */
public class RemoveTimeCounterFromPermanentOrSuspendedCardCostHandler {

    private final GameLogService gameLogService;
    private final RemoveTimeCounterFromExiledCardEffectHandler exiledCardEffectHandler;

    public RemoveTimeCounterFromPermanentOrSuspendedCardCostHandler(
            GameLogService gameLogService,
            RemoveTimeCounterFromExiledCardEffectHandler exiledCardEffectHandler) {
        this.gameLogService = gameLogService;
        this.exiledCardEffectHandler = exiledCardEffectHandler;
    }

    public void validateCanPay(GameData gameData, UUID playerId) {
        if (validCardIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException(
                    "No permanent you control or suspended card you own has a time counter to remove");
        }
    }

    public List<UUID> validCardIds(GameData gameData, UUID playerId) {
        List<UUID> cardIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (permanent.getCounterCount(CounterType.TIME) > 0) {
                cardIds.add(permanent.getCard().getId());
            }
        }
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry entry : gameData.exiledCards) {
                Integer counters = gameData.exiledCardTimeCounters.get(entry.card().getId());
                if (playerId.equals(entry.ownerId()) && counters != null && counters > 0) {
                    cardIds.add(entry.card().getId());
                }
            }
        }
        return cardIds;
    }

    public void validateAndPay(GameData gameData, Player player, UUID chosenCardId) {
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(player.getId(), List.of())) {
            if (permanent.getCard().getId().equals(chosenCardId)
                    && permanent.getCounterCount(CounterType.TIME) > 0) {
                permanent.setCounterCount(
                        CounterType.TIME, permanent.getCounterCount(CounterType.TIME) - 1);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " removes a time counter from ",
                        permanent.getCard(), " as a cost."));
                return;
            }
        }

        ExiledCardEntry exiledEntry = gameData.findExiledCard(chosenCardId);
        Integer counters = gameData.exiledCardTimeCounters.get(chosenCardId);
        if (exiledEntry != null && player.getId().equals(exiledEntry.ownerId())
                && counters != null && counters > 0) {
            int stackSizeBefore = gameData.stack.size();
            exiledCardEffectHandler.removeTimeCounter(gameData, chosenCardId);
            if (gameData.stack.size() > stackSizeBefore) {
                gameData.pendingActivatedAbilityCostTriggers.addAll(
                        new ArrayList<>(gameData.stack.subList(stackSizeBefore, gameData.stack.size())));
                gameData.stack.subList(stackSizeBefore, gameData.stack.size()).clear();
            }
            return;
        }

        throw new IllegalStateException("Chosen card no longer has a time counter to remove");
    }
}
