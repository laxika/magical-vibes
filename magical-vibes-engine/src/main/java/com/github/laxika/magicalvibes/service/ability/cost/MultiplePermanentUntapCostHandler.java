package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link UntapMultiplePermanentsCost} — the player must untap N tapped permanents
 * matching a {@link com.github.laxika.magicalvibes.model.filter.PermanentPredicate} filter
 * (e.g. "Untap two tapped blue creatures you control", Crackleburr). The untap-symbol mirror of
 * {@link MultiplePermanentTapCostHandler}: valid choices are tapped rather than untapped permanents.
 */
public class MultiplePermanentUntapCostHandler implements PermanentChoiceCostHandler {

    private final UntapMultiplePermanentsCost cost;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final UUID sourcePermanentId;

    public MultiplePermanentUntapCostHandler(UntapMultiplePermanentsCost cost, PredicateEvaluationService predicateEvaluationService,
                                             GameBroadcastService gameBroadcastService, UUID sourcePermanentId) {
        this.cost = cost;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameBroadcastService = gameBroadcastService;
        this.sourcePermanentId = sourcePermanentId;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return cost.count(); }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.size() < cost.count()) {
            throw new IllegalStateException("Not enough tapped permanents to untap (need " + cost.count() + ", have " + validIds.size() + ")");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(Permanent::isTapped)
                .filter(p -> !cost.excludeSource() || !p.getId().equals(sourcePermanentId))
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!chosen.isTapped()) {
            throw new IllegalStateException("Permanent is not tapped");
        }
        if (cost.excludeSource() && chosen.getId().equals(sourcePermanentId)) {
            throw new IllegalStateException("Cannot untap the source permanent for this cost");
        }
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, chosen, cost.filter())) {
            throw new IllegalStateException("Permanent does not match the required predicate");
        }
        chosen.untap();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " untaps " , chosen.getCard(), " as a cost."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a tapped permanent to untap (" + remaining + " remaining).";
    }
}
