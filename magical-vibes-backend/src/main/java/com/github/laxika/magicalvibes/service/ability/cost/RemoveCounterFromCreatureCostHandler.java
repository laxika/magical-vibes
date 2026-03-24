package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link RemoveCounterFromControlledCreatureCost} — the player chooses a creature
 * they control that has enough counters of the specified type, and the counter(s) are removed.
 */
public class RemoveCounterFromCreatureCostHandler implements PermanentChoiceCostHandler {

    private final RemoveCounterFromControlledCreatureCost cost;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    public RemoveCounterFromCreatureCostHandler(RemoveCounterFromControlledCreatureCost cost,
                                                 GameQueryService gameQueryService,
                                                 GameBroadcastService gameBroadcastService) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return 1; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No creature with a " + counterLabel() + " counter to remove");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> getCounterCount(p) >= cost.count())
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Must choose a creature");
        }
        int available = getCounterCount(chosen);
        if (available < cost.count()) {
            throw new IllegalStateException("Not enough " + counterLabel() + " counters on " + chosen.getCard().getName());
        }

        removeCounters(chosen, cost.count());

        String counterWord = cost.count() == 1
                ? "a " + counterLabel() + " counter"
                : cost.count() + " " + counterLabel() + " counters";
        String log = player.getUsername() + " removes " + counterWord + " from " + chosen.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, log);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a creature to remove a " + counterLabel() + " counter from.";
    }

    private int getCounterCount(Permanent permanent) {
        return switch (cost.counterType()) {
            case PLUS_ONE_PLUS_ONE -> permanent.getPlusOnePlusOneCounters();
            case MINUS_ONE_MINUS_ONE -> permanent.getMinusOneMinusOneCounters();
            case CHARGE -> permanent.getChargeCounters();
            case ANY -> permanent.getPlusOnePlusOneCounters() + permanent.getMinusOneMinusOneCounters();
            default -> 0;
        };
    }

    private void removeCounters(Permanent permanent, int count) {
        switch (cost.counterType()) {
            case PLUS_ONE_PLUS_ONE -> permanent.setPlusOnePlusOneCounters(permanent.getPlusOnePlusOneCounters() - count);
            case MINUS_ONE_MINUS_ONE -> permanent.setMinusOneMinusOneCounters(permanent.getMinusOneMinusOneCounters() - count);
            case CHARGE -> permanent.setChargeCounters(permanent.getChargeCounters() - count);
            case ANY -> {
                if (permanent.getMinusOneMinusOneCounters() >= count) {
                    permanent.setMinusOneMinusOneCounters(permanent.getMinusOneMinusOneCounters() - count);
                } else {
                    permanent.setPlusOnePlusOneCounters(permanent.getPlusOnePlusOneCounters() - count);
                }
            }
            default -> { }
        }
    }

    private String counterLabel() {
        return switch (cost.counterType()) {
            case PLUS_ONE_PLUS_ONE -> "+1/+1";
            case MINUS_ONE_MINUS_ONE -> "-1/-1";
            case CHARGE -> "charge";
            default -> "";
        };
    }
}
