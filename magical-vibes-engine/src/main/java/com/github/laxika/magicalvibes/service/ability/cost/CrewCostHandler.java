package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantCrewEffect;
import com.github.laxika.magicalvibes.model.effect.PowerBasedTapCost;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles power-based tap costs — the player must tap untapped creatures they control
 * with total power &ge; the required threshold. Unlike fixed-count handlers, this
 * handler uses power-based completion: {@link #requiredCount()} returns the power
 * threshold and {@link #lastPaymentWeight()} returns the power of the last tapped creature,
 * so the framework's {@code remaining} field tracks the power deficit.
 */
public class CrewCostHandler implements PermanentChoiceCostHandler {

    private final PowerBasedTapCost cost;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final UUID sourcePermanentId;
    private int lastTappedCreaturePower = 1;

    public CrewCostHandler(PowerBasedTapCost cost, GameQueryService gameQueryService,
                           GameLogService gameLogService,
                           TriggerCollectionService triggerCollectionService,
                           UUID sourcePermanentId) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
        this.sourcePermanentId = sourcePermanentId;
    }

    @Override public CardEffect costEffect() { return cost; }

    /**
     * Returns the required power. The framework uses this as the initial {@code remaining}
     * value, which then decreases by {@link #lastPaymentWeight()} after each creature is tapped.
     */
    @Override public int requiredCount() { return cost.requiredPower(); }

    @Override public int lastPaymentWeight() { return lastTappedCreaturePower; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        int totalPower = computeTotalAvailablePower(gameData, playerId);
        if (totalPower < cost.requiredPower()) {
            throw new IllegalStateException("Not enough creature power to " + cost.paymentNoun() + " (need "
                    + cost.requiredPower() + ", have " + totalPower + ")");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> !p.isTapped())
                // "Other untapped creatures" excludes the source permanent.
                .filter(p -> sourcePermanentId == null || !p.getId().equals(sourcePermanentId))
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> !gameQueryService.hasAuraWithEffect(gameData, p, EnchantedCreatureCantCrewEffect.class))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (chosen.isTapped()) {
            throw new IllegalStateException("Creature is already tapped");
        }
        if (sourcePermanentId != null && chosen.getId().equals(sourcePermanentId)) {
            throw new IllegalStateException("The source permanent cannot pay this cost");
        }
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Permanent is not a creature");
        }
        if (gameQueryService.hasAuraWithEffect(gameData, chosen, EnchantedCreatureCantCrewEffect.class)) {
            throw new IllegalStateException("Creature can't crew Vehicles");
        }
        Permanent sourcePermanent = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        lastTappedCreaturePower = Math.max(0,
                gameQueryService.getEffectivePowerForCrewOrSaddle(gameData, sourcePermanent, chosen));
        chosen.tap();
        if (cost instanceof CrewCost && sourcePermanentId != null) {
            gameData.recordCreatureCrewingPermanent(sourcePermanentId, chosen.getId());
        } else if (cost instanceof SaddleCost && sourcePermanentId != null) {
            gameData.recordCreatureSaddlingPermanent(sourcePermanentId, chosen.getId());
        }
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, chosen);
        triggerCollectionService.checkCrewsVehicleTriggers(gameData, chosen, sourcePermanent);
        triggerCollectionService.checkSelfSaddlesOrCrewsDuringMainPhaseTriggers(
                gameData, player.getId(), chosen, sourcePermanentId);

        gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " taps ").card(chosen.getCard()).text(" (power " + lastTappedCreaturePower + ") to " + cost.paymentNoun() + ".").build());
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a creature to tap for " + cost.paymentNoun() + " (power " + remaining + " more needed).";
    }

    @Override
    public boolean canPayRemaining(GameData gameData, UUID playerId, int remaining) {
        return computeTotalAvailablePower(gameData, playerId) >= remaining;
    }

    @Override
    public boolean shouldAutoPayAll(GameData gameData, UUID playerId, int remaining) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.isEmpty()) return true;
        if (validIds.size() == 1) return true;
        // Auto-pay all if removing any single creature would leave total power below the threshold.
        // In that case the player has no meaningful choice — all must be tapped.
        int totalPower = computeTotalAvailablePower(gameData, playerId);
        Permanent sourcePermanent = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        int minPower = validIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(Objects::nonNull)
                .mapToInt(p -> Math.max(0,
                        gameQueryService.getEffectivePowerForCrewOrSaddle(gameData, sourcePermanent, p)))
                .min().orElse(0);
        return (totalPower - minPower) < remaining;
    }

    private int computeTotalAvailablePower(GameData gameData, UUID playerId) {
        Permanent sourcePermanent = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        return getValidChoiceIds(gameData, playerId).stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(Objects::nonNull)
                .mapToInt(p -> Math.max(0,
                        gameQueryService.getEffectivePowerForCrewOrSaddle(gameData, sourcePermanent, p)))
                .sum();
    }
}
