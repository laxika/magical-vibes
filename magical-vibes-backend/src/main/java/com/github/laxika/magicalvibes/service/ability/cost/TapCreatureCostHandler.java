package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;

import java.util.List;
import java.util.UUID;

public class TapCreatureCostHandler implements PermanentChoiceCostHandler {

    private final TapCreatureCost cost;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    public TapCreatureCostHandler(TapCreatureCost cost, GameQueryService gameQueryService, GameBroadcastService gameBroadcastService) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return 1; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No untapped matching creature to tap");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> !p.isTapped())
                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, cost.predicate()))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Must tap a creature");
        }
        if (chosen.isTapped()) {
            throw new IllegalStateException("Creature is already tapped");
        }
        if (!gameQueryService.matchesPermanentPredicate(gameData, chosen, cost.predicate())) {
            throw new IllegalStateException("Creature does not match the required predicate");
        }
        chosen.tap();
        String tapLog = player.getUsername() + " taps " + chosen.getCard().getName() + " as a cost.";
        gameBroadcastService.logAndBroadcast(gameData, tapLog);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose an untapped creature to tap.";
    }
}
