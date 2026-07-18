package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PreventDamageEffect}: writes the shield-state slot selected by the effect's
 * {@code PreventionScope}. Each branch is the verbatim body of the pre-collapse per-record handler;
 * the consumption side lives unchanged in {@code DamagePreventionService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreventDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventDamageEffect) effect;
        switch (e.scope()) {
            case NEXT_TO_ANY -> nextToAny(gameData, entry, e);
            case NEXT_TO_CONTROLLER -> nextToController(gameData, entry, e);
            case NEXT_TO_SELF -> nextToSelf(gameData, entry, e);
            case NEXT_TO_TARGET -> nextToTarget(gameData, entry, e);
            case ALL_COMBAT -> {
                gameData.preventAllCombatDamage = true;
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text("All combat damage will be prevented this turn."));
            }
            case ALL_TO_CREATURES -> {
                gameData.preventAllDamageToAllCreatures = true;
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text("All damage that would be dealt to creatures this turn is prevented."));
            }
            case ALL_TO_TARGET_CREATURES -> allToTargetCreatures(gameData, entry, e);
            case ALL_BY_TARGET_CREATURES -> allByTargetCreatures(gameData, entry, e);
            case ALL_TO_CONTROLLER_AND_CREATURES -> {
                UUID controllerId = entry.getControllerId();
                gameData.playersWithAllDamagePrevented.add(controllerId);
                String playerName = gameData.playerIdToName.get(controllerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        "All damage that would be dealt to " + playerName + " and creatures " + playerName + " controls this turn is prevented."));
            }
            case ALL_TO_CONTROLLER_FROM_ATTACKERS -> {
                UUID controllerId = entry.getControllerId();
                gameData.playersWithDamageFromAttackersPrevented.add(controllerId);
                String playerName = gameData.playerIdToName.get(controllerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        "All damage that would be dealt to " + playerName + " this turn by attacking creatures is prevented."));
            }
            case ALL_FROM_COLORS -> {
                gameData.preventDamageFromColors.addAll(e.sourceColors());
                String colorNames = e.sourceColors().stream()
                        .map(c -> c.name().toLowerCase())
                        .sorted()
                        .reduce((a, b) -> a + " and " + b)
                        .orElse("");
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        "All damage from " + colorNames + " sources will be prevented this turn."));
            }
            case ALL_COMBAT_EXCEPT -> {
                gameData.combatDamageExemptPredicate = e.exemptPredicate();
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        "Combat damage from creatures that don't match the exemption will be prevented this turn."));
            }
        }
    }

    private void nextToAny(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        int amount = evaluate(gameData, entry, e);
        gameData.globalDamagePreventionShield += amount;

        String logEntry = "The next " + amount + " damage that would be dealt to any permanent or player is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - Global prevention shield increased by {}", gameData.id, amount);
    }

    private void nextToController(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;
        int amount = evaluate(gameData, entry, e);

        int currentShield = gameData.playerDamagePreventionShields.getOrDefault(controllerId, 0);
        gameData.playerDamagePreventionShields.put(controllerId, currentShield + amount);

        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "The next " + amount + " damage that would be dealt to " + controllerName + " is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - Prevention shield {} added to controller {}", gameData.id, amount, controllerName);
    }

    private void nextToSelf(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        UUID sourceId = entry.getSourcePermanentId();
        // Without the source creature on the battlefield the ability does nothing.
        if (sourceId == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) return;
        int amount = evaluate(gameData, entry, e);

        source.setDamagePreventionShield(source.getDamagePreventionShield() + amount);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                "The next " + amount + " damage that would be dealt to ", source.getCard(), " this turn is prevented."));
        log.info("Game {} - Self prevention shield {} added to permanent {}", gameData.id, amount,
                source.getCard().getName());
    }

    private void nextToTarget(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        UUID targetId = entry.getTargetId();
        int amount = evaluate(gameData, entry, e);

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            target.setDamagePreventionShield(target.getDamagePreventionShield() + amount);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    "The next " + amount + " damage that would be dealt to ", target.getCard(), " is prevented."));
            log.info("Game {} - Prevention shield {} added to permanent {}", gameData.id, amount, target.getCard().getName());
            return;
        }

        if (gameData.playerIds.contains(targetId)) {
            int currentShield = gameData.playerDamagePreventionShields.getOrDefault(targetId, 0);
            gameData.playerDamagePreventionShields.put(targetId, currentShield + amount);

            String playerName = gameData.playerIdToName.get(targetId);
            String logEntry = "The next " + amount + " damage that would be dealt to " + playerName + " is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - Prevention shield {} added to player {}", gameData.id, amount, playerName);
        }
    }

    private void allToTargetCreatures(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        // Multi-target: shield each valid creature in this effect's target group (e.g. Redeem's
        // "up to two target creatures"). Falls back to the single target for one-target spells/abilities.
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (!targetIds.isEmpty()) {
            for (UUID targetId : targetIds) {
                shieldTarget(gameData, targetId, e.combatOnly());
            }
            return;
        }

        shieldTarget(gameData, entry.getTargetId(), e.combatOnly());
    }

    private void shieldTarget(GameData gameData, UUID targetId, boolean combatOnly) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        if (combatOnly) {
            gameData.creaturesWithCombatDamagePrevented.add(targetId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    "All combat damage that would be dealt to ", target.getCard(), " this turn is prevented."));
        } else {
            gameData.creaturesWithAllDamagePrevented.add(targetId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    "All damage that would be dealt to ", target.getCard(), " this turn is prevented."));
        }
    }

    private void allByTargetCreatures(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        List<UUID> targetIds = entry.getTargetIds();
        if ((targetIds == null || targetIds.isEmpty()) && entry.getTargetId() != null) {
            // Single-target activated ability path (e.g. Resistance Fighter) stores the target
            // in the scalar targetId rather than the flat targetIds list.
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds == null || targetIds.isEmpty()) return;

        boolean combatOnly = e.combatOnly();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;

            if (combatOnly) {
                gameData.creaturesPreventedFromDealingCombatDamage.add(targetId);
            } else {
                gameData.permanentsPreventedFromDealingDamage.add(targetId);
            }
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    "All " + (combatOnly ? "combat damage " : "damage "), target.getCard(),
                    " would deal this turn is prevented."));
            log.info("Game {} - {} prevented from dealing {}damage this turn",
                    gameData.id, target.getCard().getName(), combatOnly ? "combat " : "");
        }
    }

    private int evaluate(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        return amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, null));
    }
}
