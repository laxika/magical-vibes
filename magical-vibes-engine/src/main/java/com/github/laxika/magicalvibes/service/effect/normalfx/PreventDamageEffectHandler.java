package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.DamagePreventionLifeGainShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;

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
            case NEXT_TO_ENCHANTED -> nextToEnchanted(gameData, entry, e);
            case NEXT_TO_TARGET, NEXT_TO_TARGET_CREATURE, NEXT_TO_TARGET_PLAYER_OR_PLANESWALKER ->
                    nextToTarget(gameData, entry, e);
            case NEXT_TO_TARGET_AND_SHARING_CREATURES -> nextToTargetAndSharingCreatures(gameData, entry, e);
            case NEXT_TO_EACH_CREATURE_AND_PLAYER -> nextToEachCreatureAndPlayer(gameData, entry, e);
            case ALL_COMBAT -> {
                gameData.preventAllCombatDamage = true;
                gameLogService.append(gameData, GameLog.text("All combat damage will be prevented this turn."));
            }
            case ALL_COMBAT_BY_ATTACKING_CREATURES -> {
                gameData.preventAllCombatDamageByAttackingCreatures = true;
                gameLogService.append(gameData,
                        GameLog.text("All combat damage dealt by attacking creatures will be prevented this turn."));
            }
            case ALL_COMBAT_TO_PLAYERS -> {
                gameData.preventAllCombatDamageToPlayers = true;
                gameLogService.append(gameData, GameLog.text("All combat damage that would be dealt to players will be prevented this turn."));
            }
            case ALL_TO_CREATURES -> {
                gameData.preventAllDamageToAllCreatures = true;
                gameLogService.append(gameData, GameLog.text("All damage that would be dealt to creatures this turn is prevented."));
            }
            case ALL_TO_CONTROLLED_CREATURES -> {
                UUID controllerId = entry.getControllerId();
                if (controllerId != null) {
                    gameData.playersWithAllCreatureDamagePrevented.add(controllerId);
                }
                gameLogService.append(gameData, GameLog.text(
                        "All damage that would be dealt this turn to creatures controlled by the spell's controller is prevented."));
            }
            case ALL_BY_CREATURES -> {
                gameData.preventAllDamageByCreatures = true;
                if (e.gainLife() && entry.getControllerId() != null) {
                    gameData.damageByCreaturesPreventionLifeGainPlayers.add(entry.getControllerId());
                }
                gameLogService.append(gameData, GameLog.text("All damage that would be dealt by creatures this turn is prevented."));
            }
            case ALL_TO_MATCHING_PERMANENTS -> {
                gameData.allDamagePreventionPredicates.add(e.victimPredicate());
                gameLogService.append(gameData, GameLog.text("All damage that would be dealt to the affected permanents this turn is prevented."));
            }
            case ALL_TO_CONTROLLED_MATCHING_PERMANENTS -> {
                UUID controllerId = entry.getControllerId();
                if (controllerId != null) {
                    gameData.allDamagePreventionPredicatesByController
                            .computeIfAbsent(controllerId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                            .add(e.victimPredicate());
                }
                gameLogService.append(gameData, GameLog.text(
                        "All damage that would be dealt this turn to matching permanents you control is prevented."));
            }
            case ALL_COMBAT_TO_CONTROLLED_MATCHING_PERMANENTS -> {
                UUID controllerId = entry.getControllerId();
                if (controllerId != null) {
                    gameData.combatDamagePreventionPredicatesByController
                            .computeIfAbsent(controllerId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                            .add(e.victimPredicate());
                }
                gameLogService.append(gameData, GameLog.text(
                        "All combat damage that would be dealt this turn to matching permanents you control is prevented."));
            }
            case ALL_TO_TARGET_CREATURES -> allToTargetCreatures(gameData, entry, e);
            case ALL_TO_TARGET_CREATURES_AND_ADD_PLUS_ONE_PLUS_ONE_COUNTERS ->
                    allToTargetCreaturesAndAddPlusOnePlusOneCounters(gameData, entry, e);
            case ALL_BY_TARGET_CREATURES -> allByTargetCreatures(gameData, entry, e);
            case ALL_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN -> allByTargetPermanentUntilNextTurn(gameData, entry);
            case ALL_TO_AND_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN ->
                    allToAndByTargetPermanentUntilNextTurn(gameData, entry);
            case ALL_TO_SELF -> allToSelf(gameData, entry, e.combatOnly());
            case ALL_BY_SELF -> allBySelf(gameData, entry, e.combatOnly());
            case ALL_TO_CONTROLLER_AND_CREATURES -> {
                UUID controllerId = entry.getControllerId();
                gameData.playersWithAllDamagePrevented.add(controllerId);
                String playerName = gameData.playerIdToName.get(controllerId);
                gameLogService.append(gameData, GameLog.text(
                        "All damage that would be dealt to " + playerName + " and creatures " + playerName + " controls this turn is prevented."));
            }
            case ALL_TO_CONTROLLER -> {
                UUID controllerId = entry.getControllerId();
                gameData.playersWithAllPlayerDamagePrevented.add(controllerId);
                gameLogService.append(gameData, GameLog.text("All damage that would be dealt to "
                        + gameData.playerIdToName.get(controllerId) + " this turn is prevented."));
            }
            case ALL_TO_CONTROLLER_UNTIL_NEXT_TURN -> {
                UUID controllerId = entry.getControllerId();
                gameData.playersWithAllPlayerDamagePreventedUntilNextTurn.add(controllerId);
                gameLogService.append(gameData, GameLog.text("All damage that would be dealt to "
                        + gameData.playerIdToName.get(controllerId) + " until their next turn is prevented."));
            }
            case ALL_TO_CONTROLLER_FROM_ATTACKERS -> {
                UUID controllerId = entry.getControllerId();
                gameData.playersWithDamageFromAttackersPrevented.add(controllerId);
                String playerName = gameData.playerIdToName.get(controllerId);
                gameLogService.append(gameData, GameLog.text(
                        "All damage that would be dealt to " + playerName + " this turn by attacking creatures is prevented."));
            }
            case ALL_TO_CONTROLLER_FROM_MATCHING_SOURCES -> {
                UUID controllerId = entry.getControllerId();
                if (controllerId != null) {
                    gameData.playersWithDamageFromMatchingSourcesPrevented
                            .computeIfAbsent(controllerId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                            .add(e.sourcePredicate());
                }
                gameLogService.append(gameData, GameLog.text(
                        "All damage that would be dealt to " + gameData.playerIdToName.get(controllerId)
                                + " this turn by matching creatures is prevented."));
            }
            case ALL_TO_PLAYERS_FROM_MATCHING_SOURCES -> {
                for (UUID playerId : gameData.orderedPlayerIds) {
                    gameData.playersWithDamageFromMatchingSourcesPrevented
                            .computeIfAbsent(playerId, ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                            .add(e.sourcePredicate());
                }
                gameLogService.append(gameData, GameLog.text(
                        "All damage that would be dealt to players this turn by matching sources is prevented."));
            }
            case ALL_FROM_COLORS -> {
                gameData.preventDamageFromColors.addAll(e.sourceColors());
                String colorNames = e.sourceColors().stream()
                        .map(c -> c.name().toLowerCase())
                        .sorted()
                        .reduce((a, b) -> a + " and " + b)
                        .orElse("");
                gameLogService.append(gameData, GameLog.text(
                        "All damage from " + colorNames + " sources will be prevented this turn."));
            }
            case ALL_FROM_COLORS_TO_CONTROLLED_CREATURES -> allFromColorsToControlledCreatures(gameData, entry, e);
            case ALL_FROM_CHOSEN_COLOR -> allFromChosenColor(gameData, entry);
            case ALL_FROM_NON_HUMAN_SOURCES -> {
                gameData.preventAllDamageFromNonHumanSources = true;
                gameLogService.append(gameData, GameLog.text(
                        "All damage from non-Human sources will be prevented this turn."));
            }
            case ALL_COMBAT_EXCEPT -> {
                var exemptPredicate = e.exemptPredicate();
                if (exemptPredicate instanceof PermanentIsSourcePermanentPredicate
                        && entry.getSourcePermanentId() != null) {
                    exemptPredicate = new PermanentIsSpecificPermanentPredicate(entry.getSourcePermanentId());
                }
                gameData.combatDamageExemptPredicate = exemptPredicate;
                gameData.combatDamageExemptControllerId = entry.getControllerId();
                gameLogService.append(gameData, GameLog.text(
                        "Combat damage from creatures that don't match the exemption will be prevented this turn."));
            }
            case ALL_COMBAT_EXCEPT_TARGET -> allCombatExceptTarget(gameData, entry);
        }
    }

    private void allFromColorsToControlledCreatures(GameData gameData, StackEntry entry, PreventDamageEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                gameData.colorDamagePreventionUntilEndOfTurn
                        .computeIfAbsent(permanent.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                        .addAll(effect.sourceColors());
            }
        }

        String colorNames = effect.sourceColors().stream()
                .map(color -> color.name().toLowerCase())
                .sorted()
                .reduce((first, second) -> first + " and " + second)
                .orElse("");
        gameLogService.append(gameData, GameLog.text(
                "All damage from " + colorNames + " sources that would be dealt to creatures "
                        + gameData.playerIdToName.get(controllerId) + " controls will be prevented this turn."));
    }

    private void allFromChosenColor(GameData gameData, StackEntry entry) {
        if (gameData.chosenSpellColor == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellColorChoice(gameData, entry.getControllerId());
            return;
        }

        CardColor chosenColor = gameData.chosenSpellColor;
        gameData.chosenSpellColor = null;
        gameData.rerunCurrentEffectAfterInteraction = false;
        gameData.preventDamageFromColors.add(chosenColor);
        gameLogService.append(gameData, GameLog.text(
                "All damage from " + chosenColor.name().toLowerCase()
                        + " sources will be prevented this turn."));
    }

    private void nextToAny(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        int amount = evaluate(gameData, entry, e);
        gameData.globalDamagePreventionShield += amount;

        String logEntry = "The next " + amount + " damage that would be dealt to any permanent or player is prevented.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - Global prevention shield increased by {}", gameData.id, amount);
    }

    private void nextToController(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;
        int amount = evaluate(gameData, entry, e);

        var shields = e.combatOnly()
                ? gameData.playerCombatDamagePreventionShields
                : gameData.playerDamagePreventionShields;
        int currentShield = shields.getOrDefault(controllerId, 0);
        shields.put(controllerId, currentShield + amount);

        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "The next " + amount + (e.combatOnly() ? " combat damage " : " damage ")
                + "that would be dealt to " + controllerName + " is prevented.";
        gameLogService.append(gameData, GameLog.text(logEntry));
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

        gameLogService.append(gameData, GameLog.textCardText(
                "The next " + amount + " damage that would be dealt to ", source.getCard(), " this turn is prevented."));
        log.info("Game {} - Self prevention shield {} added to permanent {}", gameData.id, amount,
                source.getCard().getName());
    }

    private void nextToEnchanted(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) return;

        Permanent aura = gameQueryService.findPermanentById(gameData, sourceId);
        if (aura == null || aura.getAttachedTo() == null) return;

        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) return;
        int amount = evaluate(gameData, entry, e);

        enchanted.setDamagePreventionShield(enchanted.getDamagePreventionShield() + amount);

        gameLogService.append(gameData, GameLog.textCardText(
                "The next " + amount + " damage that would be dealt to ", enchanted.getCard(), " this turn is prevented."));
        log.info("Game {} - Enchanted prevention shield {} added to permanent {}", gameData.id, amount,
                enchanted.getCard().getName());
    }

    private void nextToEachCreatureAndPlayer(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        int amount = evaluate(gameData, entry, e);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    permanent.setDamagePreventionShield(permanent.getDamagePreventionShield() + amount);
                }
            }
        });

        for (UUID playerId : gameData.orderedPlayerIds) {
            int currentShield = gameData.playerDamagePreventionShields.getOrDefault(playerId, 0);
            gameData.playerDamagePreventionShields.put(playerId, currentShield + amount);
        }

        gameLogService.append(gameData, GameLog.text(
                "The next " + amount + " damage that would be dealt to each creature and each player this turn is prevented."));
        log.info("Game {} - Prevention shield {} added to every creature and player", gameData.id, amount);
    }

    private void nextToTarget(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        UUID targetId = entry.getTargetId();
        int amount = evaluate(gameData, entry, e);

        if (e.gainLife()) {
            gameData.damagePreventionLifeGainShields.add(new DamagePreventionLifeGainShield(
                    targetId, entry.getControllerId(), amount));
            gameLogService.append(gameData, GameLog.text(
                    "The next " + amount + " damage that would be dealt to the target is prevented; "
                            + "the controller gains life equal to the damage prevented."));
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            target.setDamagePreventionShield(target.getDamagePreventionShield() + amount);

            gameLogService.append(gameData, GameLog.textCardText(
                    "The next " + amount + " damage that would be dealt to ", target.getCard(), " is prevented."));
            log.info("Game {} - Prevention shield {} added to permanent {}", gameData.id, amount, target.getCard().getName());
            return;
        }

        if (gameData.playerIds.contains(targetId)) {
            int currentShield = gameData.playerDamagePreventionShields.getOrDefault(targetId, 0);
            gameData.playerDamagePreventionShields.put(targetId, currentShield + amount);

            String playerName = gameData.playerIdToName.get(targetId);
            String logEntry = "The next " + amount + " damage that would be dealt to " + playerName + " is prevented.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - Prevention shield {} added to player {}", gameData.id, amount, playerName);
        }
    }

    private void nextToTargetAndSharingCreatures(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        UUID targetId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) return;

        int amount = evaluate(gameData, entry, e);
        var targetColors = gameQueryService.getEffectiveColors(gameData, target);
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> permanent.getId().equals(targetId)
                        || (!targetColors.isEmpty()
                        && gameQueryService.getEffectiveColors(gameData, permanent).stream().anyMatch(targetColors::contains)))
                .forEach(permanent -> permanent.setDamagePreventionShield(
                        permanent.getDamagePreventionShield() + amount)));

        gameLogService.append(gameData, GameLog.text(
                "The next " + amount + " damage that would be dealt to the target creature and each creature sharing a color with it this turn is prevented."));
    }

    private void allCombatExceptTarget(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return;

        gameData.combatDamageExemptPredicate = new PermanentIsSpecificPermanentPredicate(targetId);
        gameLogService.append(gameData, GameLog.textCardText(
                "All combat damage that would be dealt by creatures other than ", target.getCard(),
                " this turn is prevented."));
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

    private void allToTargetCreaturesAndAddPlusOnePlusOneCounters(
            GameData gameData, StackEntry entry, PreventDamageEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            target.setAllDamageToPlusOnePlusOneCounterPreventionShield(true);
            gameLogService.append(gameData, GameLog.textCardText(
                    "All damage that would be dealt to ", target.getCard(),
                    " this turn is prevented; a +1/+1 counter is put on it for each 1 damage prevented this way."));
        }
    }

    private void shieldTarget(GameData gameData, UUID targetId, boolean combatOnly) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        if (combatOnly) {
            gameData.creaturesWithCombatDamagePrevented.add(targetId);
            gameLogService.append(gameData, GameLog.textCardText(
                    "All combat damage that would be dealt to ", target.getCard(), " this turn is prevented."));
        } else {
            gameData.creaturesWithAllDamagePrevented.add(targetId);
            gameLogService.append(gameData, GameLog.textCardText(
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
            gameLogService.append(gameData, GameLog.textCardText(
                    "All " + (combatOnly ? "combat damage " : "damage "), target.getCard(),
                    " would deal this turn is prevented."));
            log.info("Game {} - {} prevented from dealing {}damage this turn",
                    gameData.id, target.getCard().getName(), combatOnly ? "combat " : "");
        }
    }

    private void allByTargetPermanentUntilNextTurn(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (targetId == null || controllerId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return;

        gameData.permanentsPreventedFromDealingDamageUntilNextTurn.put(targetId, controllerId);
        gameLogService.append(gameData, GameLog.textCardText(
                "All damage ", target.getCard(), " would deal is prevented until its controller's next turn."));
        log.info("Game {} - {} prevented from dealing damage until next turn", gameData.id, target.getCard().getName());
    }

    private void allToAndByTargetPermanentUntilNextTurn(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (targetId == null || controllerId == null) return;

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return;

        gameData.permanentsPreventedFromDealingDamageUntilNextTurn.put(targetId, controllerId);
        gameData.permanentsProtectedFromDamageUntilNextTurn.put(targetId, controllerId);
        gameLogService.append(gameData, GameLog.textCardText(
                "All damage that would be dealt to and dealt by ", target.getCard(),
                " is prevented until your next turn."));
        log.info("Game {} - {} damage to and by {} prevented until next turn",
                gameData.id, target.getCard().getName(), target.getCard().getName());
    }

    private void allToSelf(GameData gameData, StackEntry entry, boolean combatOnly) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) return;

        if (combatOnly) {
            gameData.creaturesWithCombatDamagePrevented.add(sourceId);
            gameLogService.append(gameData, GameLog.textCardText(
                    "All combat damage that would be dealt to ", source.getCard(), " this turn is prevented."));
            log.info("Game {} - all combat damage to {} prevented this turn", gameData.id, source.getCard().getName());
        } else {
            gameData.creaturesWithAllDamagePrevented.add(sourceId);
            gameLogService.append(gameData, GameLog.textCardText(
                    "All damage that would be dealt to ", source.getCard(), " this turn is prevented."));
            log.info("Game {} - all damage to {} prevented this turn", gameData.id, source.getCard().getName());
        }
    }

    private void allBySelf(GameData gameData, StackEntry entry, boolean combatOnly) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) return;

        if (combatOnly) {
            gameData.creaturesPreventedFromDealingCombatDamage.add(sourceId);
        } else {
            gameData.permanentsPreventedFromDealingDamage.add(sourceId);
        }
        gameLogService.append(gameData, GameLog.textCardText(
                "All " + (combatOnly ? "combat damage " : "damage "), source.getCard(),
                " would deal this turn is prevented."));
        log.info("Game {} - {} prevented from dealing {}damage this turn",
                gameData.id, source.getCard().getName(), combatOnly ? "combat " : "");
    }

    private int evaluate(GameData gameData, StackEntry entry, PreventDamageEffect e) {
        return amountEvaluationService.evaluate(gameData, e.amount(), AmountContext.forStackEntry(entry, null));
    }
}
