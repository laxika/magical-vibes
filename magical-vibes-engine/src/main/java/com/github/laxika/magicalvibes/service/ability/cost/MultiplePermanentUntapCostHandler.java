package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.ArrayList;
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
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final UUID sourcePermanentId;

    public MultiplePermanentUntapCostHandler(UntapMultiplePermanentsCost cost, PredicateEvaluationService predicateEvaluationService,
                                             GameLogService gameLogService, GameQueryService gameQueryService,
                                             UUID sourcePermanentId) {
        this.cost = cost;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
        this.gameQueryService = gameQueryService;
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
        return candidateBattlefields(gameData, playerId).stream()
                .filter(Permanent::isTapped)
                .filter(p -> !gameQueryService.cantBecomeUntapped(gameData, p))
                .filter(p -> !cost.excludeSource() || !p.getId().equals(sourcePermanentId))
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                .map(Permanent::getId)
                .toList();
    }

    /**
     * The permanents the cost may be paid from: opponents' battlefields for an
     * {@code opponentControlled} cost, otherwise the activating player's own.
     */
    private List<Permanent> candidateBattlefields(GameData gameData, UUID playerId) {
        if (!cost.opponentControlled()) {
            List<Permanent> own = gameData.playerBattlefields.get(playerId);
            return own == null ? List.of() : own;
        }
        List<Permanent> candidates = new ArrayList<>();
        for (UUID ownerId : gameData.orderedPlayerIds) {
            if (ownerId.equals(playerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(ownerId);
            if (battlefield != null) {
                candidates.addAll(battlefield);
            }
        }
        return candidates;
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (cost.opponentControlled() && !candidateBattlefields(gameData, player.getId()).contains(chosen)) {
            throw new IllegalStateException("Must choose a permanent an opponent controls");
        }
        if (!chosen.isTapped()) {
            throw new IllegalStateException("Permanent is not tapped");
        }
        if (gameQueryService.cantBecomeUntapped(gameData, chosen)) {
            throw new IllegalStateException("Permanent can't become untapped");
        }
        if (cost.excludeSource() && chosen.getId().equals(sourcePermanentId)) {
            throw new IllegalStateException("Cannot untap the source permanent for this cost");
        }
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, chosen, cost.filter())) {
            throw new IllegalStateException("Permanent does not match the required predicate");
        }
        chosen.untap();
        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " untaps " , chosen.getCard(), " as a cost."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a tapped permanent to untap (" + remaining + " remaining).";
    }
}
