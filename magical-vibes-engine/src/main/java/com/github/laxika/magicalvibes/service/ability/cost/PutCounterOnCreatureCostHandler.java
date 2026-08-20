package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link PutCounterOnControlledCreatureCost} as an activated-ability cost (e.g. Hatchet
 * Bully — "Put a -1/-1 counter on a creature you control"). The player chooses a creature they
 * control and the counter(s) are placed on it; the creature dies later via state-based actions if
 * its toughness reaches 0.
 */
public class PutCounterOnCreatureCostHandler implements PermanentChoiceCostHandler {

    private final PutCounterOnControlledCreatureCost cost;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    public PutCounterOnCreatureCostHandler(PutCounterOnControlledCreatureCost cost,
                                           GameQueryService gameQueryService,
                                           GameLogService gameLogService) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return 1; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No creature you control to put a " + counterLabel() + " counter on");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Must choose a creature");
        }
        CounterType type = cost.counterType();
        int count = cost.count();
        count = gameQueryService.replaceCounters(gameData, chosen, type, count, player.getId());
        if (count <= 0) {
            return;
        }
        chosen.setCounterCount(type, chosen.getCounterCount(type) + count);
        gameData.playersWhoPutCountersOnCreaturesThisTurn.add(player.getId());
        if (type == CounterType.PLUS_ONE_PLUS_ONE) {
            gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(player.getId());
        }

        String counterWord = count == 1
                ? "a " + counterLabel() + " counter"
                : count + " " + counterLabel() + " counters";
        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " puts " + counterWord + " on " , chosen.getCard(), "."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a creature to put a " + counterLabel() + " counter on.";
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
