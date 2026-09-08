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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the whole life-loss family via {@link LoseLifeEffect}: the {@code LoseLifeRecipient}
 * routes who loses life and {@code controllerGainsLifeLost} drains the total life lost back to the
 * controller. The {@link DynamicAmount} amount is evaluated once against the stack entry (source-
 * relative amounts use the live source permanent when present, else the last-known snapshot).
 *
 * <p>Every recipient goes through {@link LifeSupport#applyLifeLoss}, which records the amount lost
 * and fires "loses life" triggers. Life loss is never routed through damage plumbing.
 */
@Component
@RequiredArgsConstructor
public class LoseLifeEffectHandler implements NormalEffectHandlerBean {

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
        if (ownerId != null) {
            amountContext = amountContext.withControllerId(ownerId);
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), amountContext);

        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();

        switch (e.recipient()) {
            case CONTROLLER -> lifeSupport.applyLifeLoss(gameData, controllerId, amount, sourceName);
            case OWNER -> lifeSupport.applyLifeLoss(gameData, ownerId, amount, sourceName);
            case TARGET_PLAYER, TRIGGERING_PLAYER, ACTIVE_PLAYER -> loseTargetPlayerLife(gameData, entry, e, amount, sourceName);
            case TARGET_PERMANENT_CONTROLLER -> loseTargetPermanentControllerLife(gameData, entry, amount, sourceName);
            case DYING_CREATURE_CONTROLLER -> dyingCreatureControllerLosesLife(gameData, entry, amount, sourceName);
            case DEFENDING_PLAYER -> defendingPlayerLosesLife(gameData, amount, sourceName, defendingPlayerId);
            case EACH_PLAYER -> eachPlayerLosesLife(gameData, e, entry, controllerId, amount, sourceName, false);
            case EACH_OPPONENT -> eachPlayerLosesLife(gameData, e, entry, controllerId, amount, sourceName, true);
        }
    }

    private UUID defendingPlayerId(GameData gameData, StackEntry entry) {
        // The attacked player/planeswalker was baked onto the combat trigger as attackedTargetId.
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }

    private void defendingPlayerLosesLife(GameData gameData, int amount, String sourceName,
                                           UUID defendingPlayerId) {
        if (defendingPlayerId != null) {
            lifeSupport.applyLifeLoss(gameData, defendingPlayerId, amount, sourceName);
        }
    }

    private void dyingCreatureControllerLosesLife(GameData gameData, StackEntry entry, int amount, String sourceName) {
        // The creature is already in the graveyard by the time the trigger resolves, so the
        // graveyard pipeline baked its last-known controller onto the trigger's targetId.
        UUID dyingControllerId = entry.getTargetId();
        if (dyingControllerId != null) {
            lifeSupport.applyLifeLoss(gameData, dyingControllerId, amount, sourceName);
        }
    }

    private void loseTargetPlayerLife(GameData gameData, StackEntry entry, LoseLifeEffect effect,
            int amount, String sourceName) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null && entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            targetPlayerId = entry.getTargetIds().getFirst();
        }
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        int lifeBefore = gameData.getLife(targetPlayerId);
        lifeSupport.applyLifeLoss(gameData, targetPlayerId, amount, sourceName);
        int lifeLost = Math.max(0, lifeBefore - gameData.getLife(targetPlayerId));
        if (controllerGainsLifeLost(gameData, entry, effect) && lifeLost > 0) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), lifeLost);
        }
    }

    private void loseTargetPermanentControllerLife(GameData gameData, StackEntry entry, int amount, String sourceName) {
        // targetId is the targeted permanent; the controller of that permanent loses life. Runs
        // before any accompanying destroy effect so the permanent is still on the battlefield.
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        lifeSupport.applyLifeLoss(gameData, controllerId, amount, sourceName);
    }

    private boolean controlsMatching(GameData gameData, UUID playerId, PermanentPredicate predicate) {
        var battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, predicate));
    }

    private void eachPlayerLosesLife(GameData gameData, LoseLifeEffect e, StackEntry entry,
            UUID controllerId, int amount, String sourceName, boolean opponentsOnly) {
        // The X-scaled drain (Exsanguinate) short-circuits on non-positive X before touching any
        // life total — preserves the former EachOpponentLosesXLife... early-out.
        if (controllerGainsLifeLost(gameData, entry, e) && amount <= 0) {
            return;
        }

        int totalLifeLost = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (opponentsOnly && playerId.equals(controllerId)) {
                continue;
            }
            // "each opponent who doesn't control an Elf" — players controlling a matching
            // permanent are skipped entirely (Thornbow Archer).
            if (e.exemptIfControls() != null && controlsMatching(gameData, playerId, e.exemptIfControls())) {
                continue;
            }
            lifeSupport.applyLifeLoss(gameData, playerId, amount, sourceName);
            totalLifeLost += amount * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
        }

        if (controllerGainsLifeLost(gameData, entry, e) && totalLifeLost > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, totalLifeLost);
        }
    }

    private boolean controllerGainsLifeLost(GameData gameData, StackEntry entry, LoseLifeEffect effect) {
        return effect.controllerGainsLifeLost()
                && (effect.controllerGainsLifeLostCondition() == null
                || conditionEvaluationService.isMet(gameData, effect.controllerGainsLifeLostCondition(),
                ConditionContext.forStackEntry(entry)));
    }
}
