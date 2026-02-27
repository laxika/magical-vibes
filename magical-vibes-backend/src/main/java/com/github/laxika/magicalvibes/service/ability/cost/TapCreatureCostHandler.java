package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link TapCreatureCost} — the player must tap an untapped creature matching a
 * {@link com.github.laxika.magicalvibes.model.filter.PermanentPredicate} (e.g. "Tap an untapped
 * blue creature you control"). Unlike the sacrifice handlers, payment taps the creature rather
 * than removing it from the battlefield.
 */
public class TapCreatureCostHandler implements PermanentChoiceCostHandler {

    private final TapCreatureCost cost;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;

    /**
     * @param cost                     the cost effect record specifying the creature predicate
     * @param gameQueryService          used to check creature status and evaluate the predicate
     * @param gameBroadcastService      used to log and broadcast the tap action
     * @param triggerCollectionService  used to fire "enchanted permanent becomes tapped" triggers
     */
    public TapCreatureCostHandler(TapCreatureCost cost, GameQueryService gameQueryService,
                                  GameBroadcastService gameBroadcastService,
                                  TriggerCollectionService triggerCollectionService) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.triggerCollectionService = triggerCollectionService;
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
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, chosen);
        String tapLog = player.getUsername() + " taps " + chosen.getCard().getName() + " as a cost.";
        gameBroadcastService.logAndBroadcast(gameData, tapLog);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose an untapped creature to tap.";
    }
}
