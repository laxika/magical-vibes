package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the whole life-loss family via {@link LoseLifeEffect}: the {@code LoseLifeRecipient}
 * routes who loses life and {@code controllerGainsLifeLost} drains the total life lost back to the
 * controller. The {@link DynamicAmount} amount is evaluated once against the stack entry (source-
 * relative amounts use the live source permanent when present, else the last-known snapshot).
 *
 * <p>Every recipient goes through {@link LifeSupport#applyLifeLosses}, which records the amount lost
 * and fires "loses life" triggers. Life loss is never routed through damage plumbing.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the life losses are its first change, so a Lich's Duel
 * Mastery one of them would sacrifice instead is asked about (CR 616.1) before any life is lost.
 */
@Component
@RequiredArgsConstructor
public class LoseLifeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LoseLifeEffect) effect;

        // Source-relative amounts (e.g. "for each Vampire you control") use the live source
        // permanent when present, else the last-known snapshot.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        UUID defendingPlayerId = e.recipient() == LoseLifeRecipient.DEFENDING_PLAYER
                ? defendingPlayerId(gameData, entry) : null;
        AmountContext amountContext = AmountContext.forStackEntry(entry, source);
        if (defendingPlayerId != null) {
            amountContext = amountContext.withTargetPermanentId(defendingPlayerId);
        }
        UUID ownerId = e.recipient() == LoseLifeRecipient.OWNER ? entry.getCard().getOwnerId() : null;
        if (e.recipient() == LoseLifeRecipient.OWNER && ownerId == null) {
            ownerId = entry.getControllerId();
        }
        UUID sourceControllerId = e.recipient() == LoseLifeRecipient.SOURCE_CONTROLLER
                ? sourcePermanentControllerId(gameData, entry) : null;
        if (ownerId != null) {
            amountContext = amountContext.withControllerId(ownerId);
        } else if (sourceControllerId != null) {
            amountContext = amountContext.withControllerId(sourceControllerId);
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), amountContext);

        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();

        Map<UUID, Integer> losses = switch (e.recipient()) {
            case CONTROLLER -> single(controllerId, amount);
            case OWNER -> single(ownerId, amount);
            case SOURCE_CONTROLLER -> single(sourceControllerId, amount);
            case TARGET_PLAYER, TRIGGERING_PLAYER -> targetPlayerLosses(gameData, entry, e, amount, amountContext);
            case ACTIVE_PLAYER -> single(entry.getActivePlayerId() != null
                    ? entry.getActivePlayerId() : gameData.activePlayerId, amount);
            case TARGET_PERMANENT_CONTROLLER -> single(targetPermanentControllerId(gameData, entry), amount);
            // The creature is already in the graveyard by the time the trigger resolves, so the
            // graveyard pipeline baked its last-known controller onto the trigger's targetId.
            case DYING_CREATURE_CONTROLLER -> single(entry.getTargetId(), amount);
            case DEFENDING_PLAYER -> single(defendingPlayerId, amount);
            case EACH_PLAYER -> eachPlayerLosses(gameData, e, entry, controllerId, amount, false);
            case EACH_OPPONENT -> eachPlayerLosses(gameData, e, entry, controllerId, amount, true);
            case EACH_OTHER_PLAYER -> eachPlayerLosses(gameData, e, entry,
                    entry.getCard().getOwnerId() != null ? entry.getCard().getOwnerId() : controllerId,
                    amount, false);
        };
        int totalLifeLost = lifeSupport.applyLifeLosses(gameData, losses, sourceName);

        boolean targeted = e.recipient() == LoseLifeRecipient.TARGET_PLAYER
                || e.recipient() == LoseLifeRecipient.TRIGGERING_PLAYER;
        if (targeted && e.recordTargetLifeLost()) {
            entry.setEventValue(totalLifeLost);
        }
        boolean drains = targeted || e.recipient() == LoseLifeRecipient.EACH_PLAYER
                || e.recipient() == LoseLifeRecipient.EACH_OPPONENT
                || e.recipient() == LoseLifeRecipient.EACH_OTHER_PLAYER;
        if (drains && totalLifeLost > 0 && controllerGainsLifeLost(gameData, entry, e)) {
            UUID gainingPlayerId = e.recipient() == LoseLifeRecipient.EACH_OTHER_PLAYER
                    && entry.getCard().getOwnerId() != null ? entry.getCard().getOwnerId() : controllerId;
            lifeSupport.applyGainLife(gameData, gainingPlayerId, totalLifeLost);
        }
    }

    private static Map<UUID, Integer> single(UUID playerId, int amount) {
        return playerId == null ? Map.of() : Map.of(playerId, amount);
    }

    private UUID sourcePermanentControllerId(GameData gameData, StackEntry entry) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            UUID liveControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
            if (liveControllerId != null) {
                return liveControllerId;
            }
        }
        return entry.getSourcePermanentControllerId();
    }

    private UUID defendingPlayerId(GameData gameData, StackEntry entry) {
        if (entry.getDefendingPlayerId() != null) return entry.getDefendingPlayerId();
        // The attacked player/planeswalker was baked onto the combat trigger as attackedTargetId.
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }

    private Map<UUID, Integer> targetPlayerLosses(GameData gameData, StackEntry entry, LoseLifeEffect effect,
            int amount, AmountContext amountContext) {
        List<UUID> targetPlayerIds = effect.recipient() == LoseLifeRecipient.TARGET_PLAYER
                ? entry.targetsForEffect(effect) : List.of();
        if (targetPlayerIds.isEmpty()) {
            UUID targetPlayerId = entry.getTargetId();
            if (targetPlayerId == null && !entry.getTargetIds().isEmpty()) {
                targetPlayerId = entry.getTargetIds().getFirst();
            }
            targetPlayerIds = targetPlayerId == null ? List.of() : List.of(targetPlayerId);
        }
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID targetPlayerId : targetPlayerIds) {
            if (!gameData.playerIds.contains(targetPlayerId)) continue;
            int targetAmount = targetPlayerIds.size() == 1 ? amount
                    : amountEvaluationService.evaluate(gameData, effect.amount(),
                    amountContext.withTargetPermanentId(targetPlayerId));
            losses.put(targetPlayerId, targetAmount);
        }
        return losses;
    }

    private UUID targetPermanentControllerId(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return null;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
        if (controllerId == null) {
            controllerId = entry.getRemovedPermanentControllers().get(targetId);
        }
        if (controllerId == null && targetId.equals(entry.getTriggeringPermanentId())) {
            controllerId = entry.getTriggeringPermanentControllerId();
        }
        return controllerId;
    }

    private boolean controlsMatching(GameData gameData, UUID playerId, PermanentPredicate predicate) {
        var battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, predicate));
    }

    private Map<UUID, Integer> eachPlayerLosses(GameData gameData, LoseLifeEffect e, StackEntry entry,
            UUID controllerId, int amount, boolean opponentsOnly) {
        // The X-scaled drain (Exsanguinate) short-circuits on non-positive X before touching any
        // life total — preserves the former EachOpponentLosesXLife... early-out.
        if (controllerGainsLifeLost(gameData, entry, e) && amount <= 0) {
            return Map.of();
        }

        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId) && (opponentsOnly || e.recipient() == LoseLifeRecipient.EACH_OTHER_PLAYER)) {
                continue;
            }
            // "each opponent who doesn't control an Elf" — players controlling a matching
            // permanent are skipped entirely (Thornbow Archer).
            if (e.exemptIfControls() != null && controlsMatching(gameData, playerId, e.exemptIfControls())) {
                continue;
            }
            losses.put(playerId, amount);
        }
        return losses;
    }

    private boolean controllerGainsLifeLost(GameData gameData, StackEntry entry, LoseLifeEffect effect) {
        return effect.controllerGainsLifeLost()
                && (effect.controllerGainsLifeLostCondition() == null
                || conditionEvaluationService.isMet(gameData, effect.controllerGainsLifeLostCondition(),
                ConditionContext.forStackEntry(entry)));
    }
}
