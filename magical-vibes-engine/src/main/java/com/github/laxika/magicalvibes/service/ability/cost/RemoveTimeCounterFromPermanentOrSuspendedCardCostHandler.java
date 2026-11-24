package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.RemoveTimeCounterFromExiledCardEffectHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Pays the mixed permanent-or-suspended-card time-counter cost. */
public class RemoveTimeCounterFromPermanentOrSuspendedCardCostHandler {

    private final GameLogService gameLogService;
    private final RemoveTimeCounterFromExiledCardEffectHandler exiledCardEffectHandler;
    private final PermanentCounterSupport permanentCounterSupport;

    public RemoveTimeCounterFromPermanentOrSuspendedCardCostHandler(
            GameLogService gameLogService,
            RemoveTimeCounterFromExiledCardEffectHandler exiledCardEffectHandler,
            PermanentCounterSupport permanentCounterSupport) {
        this.gameLogService = gameLogService;
        this.exiledCardEffectHandler = exiledCardEffectHandler;
        this.permanentCounterSupport = permanentCounterSupport;
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
                if (playerId.equals(entry.ownerId()) && gameData.isSuspended(entry.card().getId())) {
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
                int stackSizeBefore = gameData.stack.size();
                int previousCount = permanent.getCounterCount(CounterType.TIME);
                permanent.setCounterCount(CounterType.TIME, previousCount - 1);
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " removes a time counter from ",
                        permanent.getCard(), " as a cost."));
                // Vanishing's sacrifice triggers when the last time counter is removed, even as a cost.
                permanentCounterSupport.checkLastTimeCounterRemoved(gameData, permanent, CounterType.TIME,
                        previousCount);
                moveCostTriggersAside(gameData, stackSizeBefore);
                return;
            }
        }

        ExiledCardEntry exiledEntry = gameData.findExiledCard(chosenCardId);
        if (exiledEntry != null && player.getId().equals(exiledEntry.ownerId())
                && gameData.isSuspended(chosenCardId)) {
            int stackSizeBefore = gameData.stack.size();
            exiledCardEffectHandler.removeTimeCounter(gameData, chosenCardId);
            moveCostTriggersAside(gameData, stackSizeBefore);
            return;
        }

        throw new IllegalStateException("Chosen card no longer has a time counter to remove");
    }

    /**
     * Moves triggers queued while paying the cost off the stack, so they are put on the stack above the
     * ability being activated once it is put on the stack.
     */
    private void moveCostTriggersAside(GameData gameData, int stackSizeBefore) {
        if (gameData.stack.size() > stackSizeBefore) {
            gameData.pendingActivatedAbilityCostTriggers.addAll(
                    new ArrayList<>(gameData.stack.subList(stackSizeBefore, gameData.stack.size())));
            gameData.stack.subList(stackSizeBefore, gameData.stack.size()).clear();
        }
    }
}
