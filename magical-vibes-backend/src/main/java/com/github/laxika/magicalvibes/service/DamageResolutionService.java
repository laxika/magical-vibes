package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesAndPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToFlyingAndPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DamageResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(DealXDamageToTargetCreatureEffect.class,
                (gd, entry, effect) -> resolveDealXDamageToTargetCreature(gd, entry));
        registry.register(DealDamageToTargetCreatureEffect.class,
                (gd, entry, effect) -> resolveDealDamageToTargetCreature(gd, entry, (DealDamageToTargetCreatureEffect) effect));
        registry.register(DealXDamageDividedAmongTargetAttackingCreaturesEffect.class,
                (gd, entry, effect) -> resolveDealXDamageDividedAmongTargetAttackingCreatures(gd, entry));
        registry.register(DealDamageToAllCreaturesAndPlayersEffect.class,
                (gd, entry, effect) -> resolveDealDamageToAllCreaturesAndPlayers(gd, entry, (DealDamageToAllCreaturesAndPlayersEffect) effect));
        registry.register(DealDamageToFlyingAndPlayersEffect.class,
                (gd, entry, effect) -> resolveDealDamageToFlyingAndPlayers(gd, entry));
        registry.register(DealXDamageToAnyTargetEffect.class,
                (gd, entry, effect) -> resolveDealXDamageToAnyTarget(gd, entry));
        registry.register(DealXDamageToAnyTargetAndGainXLifeEffect.class,
                (gd, entry, effect) -> resolveDealXDamageToAnyTargetAndGainXLife(gd, entry));
        registry.register(DealDamageToAnyTargetEffect.class,
                (gd, entry, effect) -> resolveDealDamageToAnyTarget(gd, entry, (DealDamageToAnyTargetEffect) effect));
        registry.register(DealDamageToAnyTargetAndGainLifeEffect.class,
                (gd, entry, effect) -> resolveDealDamageToAnyTargetAndGainLife(gd, entry, (DealDamageToAnyTargetAndGainLifeEffect) effect));
        registry.register(DealDamageToControllerEffect.class,
                (gd, entry, effect) -> resolveDealDamageToController(gd, entry, (DealDamageToControllerEffect) effect));
        registry.register(DealDamageToTargetPlayerEffect.class,
                (gd, entry, effect) -> resolveDealDamageToTargetPlayer(gd, entry, (DealDamageToTargetPlayerEffect) effect));
        registry.register(DealOrderedDamageToAnyTargetsEffect.class,
                (gd, entry, effect) -> resolveDealOrderedDamageToAnyTargets(gd, entry, (DealOrderedDamageToAnyTargetsEffect) effect));
        registry.register(DealDamageIfFewCardsInHandEffect.class,
                (gd, entry, effect) -> resolveDealDamageIfFewCardsInHand(gd, entry, (DealDamageIfFewCardsInHandEffect) effect));
    }

    void resolveDealXDamageToTargetCreature(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())
                || gameQueryService.hasProtectionFrom(gameData, target, entry.getCard().getColor())) {
            String logEntry = entry.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int damage = gameHelper.applyCreaturePreventionShield(gameData, target, gameQueryService.applyDamageMultiplier(gameData, entry.getXValue()));
        String logEntry = entry.getCard().getName() + " deals " + damage + " damage to " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} deals {} damage to {}", gameData.id, entry.getCard().getName(), damage, target.getCard().getName());

        if (damage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                String indestructibleLog = target.getCard().getName() + " is indestructible and survives.";
                gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
            } else if (gameHelper.tryRegenerate(gameData, target)) {

            } else {
                gameHelper.removePermanentToGraveyard(gameData, target);
                String destroyLog = target.getCard().getName() + " is destroyed.";
                gameBroadcastService.logAndBroadcast(gameData, destroyLog);
                log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
                gameHelper.removeOrphanedAuras(gameData);
            }
        }
    }

    void resolveDealDamageToTargetCreature(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())
                || gameQueryService.hasProtectionFrom(gameData, target, entry.getCard().getColor())) {
            String logEntry = entry.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int damage = gameHelper.applyCreaturePreventionShield(gameData, target, gameQueryService.applyDamageMultiplier(gameData, effect.damage()));
        String logEntry = entry.getCard().getName() + " deals " + damage + " damage to " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} deals {} damage to {}", gameData.id, entry.getCard().getName(), damage, target.getCard().getName());

        if (damage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                String indestructibleLog = target.getCard().getName() + " is indestructible and survives.";
                gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
            } else if (gameHelper.tryRegenerate(gameData, target)) {

            } else {
                gameHelper.removePermanentToGraveyard(gameData, target);
                String destroyLog = target.getCard().getName() + " is destroyed.";
                gameBroadcastService.logAndBroadcast(gameData, destroyLog);
                log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
                gameHelper.removeOrphanedAuras(gameData);
            }
        }
    }

    void resolveDealXDamageDividedAmongTargetAttackingCreatures(GameData gameData, StackEntry entry) {
        Map<UUID, Integer> assignments = entry.getDamageAssignments();
        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = entry.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Permanent> destroyed = new ArrayList<>();

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
            if (target == null) {
                continue;
            }
            if (gameQueryService.hasProtectionFrom(gameData, target, entry.getCard().getColor())) {
                continue;
            }

            int damage = gameHelper.applyCreaturePreventionShield(gameData, target, gameQueryService.applyDamageMultiplier(gameData, assignment.getValue()));
            String logEntry = entry.getCard().getName() + " deals " + damage + " damage to " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} deals {} damage to {}", gameData.id, entry.getCard().getName(), damage, target.getCard().getName());

            if (damage >= target.getEffectiveToughness()) {
                if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                    String indestructibleLog = target.getCard().getName() + " is indestructible and survives.";
                    gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
                } else if (!gameHelper.tryRegenerate(gameData, target)) {
                    destroyed.add(target);
                }
            }
        }

        for (Permanent target : destroyed) {
            gameHelper.removePermanentToGraveyard(gameData, target);
            String destroyLog = target.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, destroyLog);
            log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
        }

        if (!destroyed.isEmpty()) {
            gameHelper.removeOrphanedAuras(gameData);
        }
    }

    void resolveDealDamageToAllCreaturesAndPlayers(GameData gameData, StackEntry entry, DealDamageToAllCreaturesAndPlayersEffect effect) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logMsg = entry.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        String cardName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            Set<Integer> deadIndices = new TreeSet<>(Collections.reverseOrder());
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent p = battlefield.get(i);
                if (!gameQueryService.isCreature(gameData, p)) {
                    continue;
                }
                if (gameQueryService.hasProtectionFrom(gameData, p, entry.getCard().getColor())) {
                    continue;
                }
                int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, p, damage);
                int toughness = gameQueryService.getEffectiveToughness(gameData, p);
                if (effectiveDamage >= toughness
                        && !gameQueryService.hasKeyword(gameData, p, Keyword.INDESTRUCTIBLE)
                        && !gameHelper.tryRegenerate(gameData, p)) {
                    deadIndices.add(i);
                }
            }

            for (int idx : deadIndices) {
                String playerName = gameData.playerIdToName.get(playerId);
                Permanent dead = battlefield.get(idx);
                String logEntry = playerName + "'s " + dead.getCard().getName() + " is destroyed by " + cardName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                gameHelper.addCardToGraveyard(gameData, playerId, dead.getOriginalCard());
                gameHelper.collectDeathTrigger(gameData, dead.getCard(), playerId, true);
                gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                battlefield.remove(idx);
            }
        }

        gameHelper.removeOrphanedAuras(gameData);

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (gameHelper.applyColorDamagePreventionForPlayer(gameData, playerId, entry.getCard().getColor())) {
                continue;
            }
            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, playerId, damage);
            effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

            if (effectiveDamage > 0) {
                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToFlyingAndPlayers(GameData gameData, StackEntry entry) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logMsg = entry.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue());
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            Set<Integer> deadIndices = new TreeSet<>(Collections.reverseOrder());
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent p = battlefield.get(i);
                if (gameQueryService.hasKeyword(gameData, p, Keyword.FLYING)) {
                    if (gameQueryService.hasProtectionFrom(gameData, p, entry.getCard().getColor())) {
                        continue;
                    }
                    int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, p, damage);
                    int toughness = gameQueryService.getEffectiveToughness(gameData, p);
                    if (effectiveDamage >= toughness
                            && !gameQueryService.hasKeyword(gameData, p, Keyword.INDESTRUCTIBLE)
                            && !gameHelper.tryRegenerate(gameData, p)) {
                        deadIndices.add(i);
                    }
                }
            }

            for (int idx : deadIndices) {
                String playerName = gameData.playerIdToName.get(playerId);
                Permanent dead = battlefield.get(idx);
                String logEntry = playerName + "'s " + dead.getCard().getName() + " is destroyed by Hurricane.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                gameHelper.addCardToGraveyard(gameData, playerId, dead.getOriginalCard());
                gameHelper.collectDeathTrigger(gameData, dead.getCard(), playerId, true);
                gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                battlefield.remove(idx);
            }
        }

        gameHelper.removeOrphanedAuras(gameData);

        String cardName = entry.getCard().getName();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (gameHelper.applyColorDamagePreventionForPlayer(gameData, playerId, entry.getCard().getColor())) {
                continue;
            }
            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, playerId, damage);
            effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

            if (effectiveDamage > 0) {
                String playerName = gameData.playerIdToName.get(playerId);
                String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealXDamageToAnyTarget(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        int xValue = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue());
        String cardName = entry.getCard().getName();

        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

        if (!targetIsPlayer && targetPermanent == null) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = cardName + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (targetIsPlayer) {
            if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, targetId, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, targetId, xValue);
                effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, targetId, effectiveDamage, cardName);
                int currentLife = gameData.playerLifeTotals.getOrDefault(targetId, 20);
                gameData.playerLifeTotals.put(targetId, currentLife - effectiveDamage);

                if (effectiveDamage > 0) {
                    String playerName = gameData.playerIdToName.get(targetId);
                    String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        } else {
            if (!gameQueryService.hasProtectionFrom(gameData, targetPermanent, entry.getCard().getColor())) {
                int damage = gameHelper.applyCreaturePreventionShield(gameData, targetPermanent, xValue);
                String logEntry = cardName + " deals " + damage + " damage to " + targetPermanent.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} deals {} damage to {}", gameData.id, cardName, damage, targetPermanent.getCard().getName());

                if (damage >= gameQueryService.getEffectiveToughness(gameData, targetPermanent)) {
                    if (gameQueryService.hasKeyword(gameData, targetPermanent, Keyword.INDESTRUCTIBLE)) {
                        String indestructibleLog = targetPermanent.getCard().getName() + " is indestructible and survives.";
                        gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
                    } else if (!gameHelper.tryRegenerate(gameData, targetPermanent)) {
                        gameHelper.removePermanentToGraveyard(gameData, targetPermanent);
                        String destroyLog = targetPermanent.getCard().getName() + " is destroyed.";
                        gameBroadcastService.logAndBroadcast(gameData, destroyLog);
                        log.info("Game {} - {} is destroyed", gameData.id, targetPermanent.getCard().getName());
                        gameHelper.removeOrphanedAuras(gameData);
                    }
                }
            } else {
                String logEntry = cardName + "'s damage is prevented.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealXDamageToAnyTargetAndGainXLife(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        int xValue = entry.getXValue();
        int damageValue = gameQueryService.applyDamageMultiplier(gameData, xValue);
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();

        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

        if (!targetIsPlayer && targetPermanent == null) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = cardName + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (targetIsPlayer) {
            if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, targetId, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, targetId, damageValue);
                effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, targetId, effectiveDamage, cardName);
                int currentLife = gameData.playerLifeTotals.getOrDefault(targetId, 20);
                gameData.playerLifeTotals.put(targetId, currentLife - effectiveDamage);

                if (effectiveDamage > 0) {
                    String playerName = gameData.playerIdToName.get(targetId);
                    String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        } else {
            if (!gameQueryService.hasProtectionFrom(gameData, targetPermanent, entry.getCard().getColor())) {
                int damage = gameHelper.applyCreaturePreventionShield(gameData, targetPermanent, damageValue);
                String logEntry = cardName + " deals " + damage + " damage to " + targetPermanent.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} deals {} damage to {}", gameData.id, cardName, damage, targetPermanent.getCard().getName());

                if (damage >= gameQueryService.getEffectiveToughness(gameData, targetPermanent)) {
                    if (gameQueryService.hasKeyword(gameData, targetPermanent, Keyword.INDESTRUCTIBLE)) {
                        String indestructibleLog = targetPermanent.getCard().getName() + " is indestructible and survives.";
                        gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
                    } else if (!gameHelper.tryRegenerate(gameData, targetPermanent)) {
                        gameHelper.removePermanentToGraveyard(gameData, targetPermanent);
                        String destroyLog = targetPermanent.getCard().getName() + " is destroyed.";
                        gameBroadcastService.logAndBroadcast(gameData, destroyLog);
                        log.info("Game {} - {} is destroyed", gameData.id, targetPermanent.getCard().getName());
                        gameHelper.removeOrphanedAuras(gameData);
                    }
                }
            } else {
                String logEntry = cardName + "'s damage is prevented.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        // Life gain is independent of damage prevention — always happens if the spell resolves
        int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
        gameData.playerLifeTotals.put(controllerId, currentLife + xValue);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String lifeLog = controllerName + " gains " + xValue + " life.";
        gameBroadcastService.logAndBroadcast(gameData, lifeLog);
        log.info("Game {} - {} gains {} life", gameData.id, controllerName, xValue);

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToTargetPlayer(GameData gameData, StackEntry entry, DealDamageToTargetPlayerEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        String cardName = entry.getCard().getName();

        if (!gameData.playerIds.contains(targetId)) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = cardName + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, targetId, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, targetId, damage);
                effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, targetId, effectiveDamage, cardName);
                int currentLife = gameData.playerLifeTotals.getOrDefault(targetId, 20);
                gameData.playerLifeTotals.put(targetId, currentLife - effectiveDamage);

                if (effectiveDamage > 0) {
                    String playerName = gameData.playerIdToName.get(targetId);
                    String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageIfFewCardsInHand(GameData gameData, StackEntry entry, DealDamageIfFewCardsInHandEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        String cardName = entry.getCard().getName();

        if (!gameData.playerIds.contains(targetId)) {
            return;
        }

        // Intervening-if: re-check condition at resolution time
        List<Card> hand = gameData.playerHands.get(targetId);
        int handSize = hand != null ? hand.size() : 0;
        if (handSize > effect.maxCards()) {
            String playerName = gameData.playerIdToName.get(targetId);
            String logEntry = cardName + "'s ability does nothing — " + playerName + " has more than " + effect.maxCards() + " cards in hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = cardName + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, targetId, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, targetId, damage);
                effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, targetId, effectiveDamage, cardName);
                int currentLife = gameData.playerLifeTotals.getOrDefault(targetId, 20);
                gameData.playerLifeTotals.put(targetId, currentLife - effectiveDamage);

                if (effectiveDamage > 0) {
                    String playerName = gameData.playerIdToName.get(targetId);
                    String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToAnyTarget(GameData gameData, StackEntry entry, DealDamageToAnyTargetEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        String cardName = entry.getCard().getName();

        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

        if (!targetIsPlayer && targetPermanent == null) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = cardName + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (targetIsPlayer) {
            if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, targetId, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, targetId, damage);
                effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, targetId, effectiveDamage, cardName);
                int currentLife = gameData.playerLifeTotals.getOrDefault(targetId, 20);
                gameData.playerLifeTotals.put(targetId, currentLife - effectiveDamage);

                if (effectiveDamage > 0) {
                    String playerName = gameData.playerIdToName.get(targetId);
                    String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        } else {
            if (!gameQueryService.hasProtectionFrom(gameData, targetPermanent, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, targetPermanent, damage);

                if (effect.cantRegenerate() && effectiveDamage > 0) {
                    targetPermanent.setCantRegenerateThisTurn(true);
                }

                String logEntry = cardName + " deals " + effectiveDamage + " damage to " + targetPermanent.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} deals {} damage to {}", gameData.id, cardName, effectiveDamage, targetPermanent.getCard().getName());

                if (effectiveDamage >= gameQueryService.getEffectiveToughness(gameData, targetPermanent)) {
                    if (gameQueryService.hasKeyword(gameData, targetPermanent, Keyword.INDESTRUCTIBLE)) {
                        String indestructibleLog = targetPermanent.getCard().getName() + " is indestructible and survives.";
                        gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
                    } else if (!gameHelper.tryRegenerate(gameData, targetPermanent)) {
                        gameHelper.removePermanentToGraveyard(gameData, targetPermanent);
                        String destroyLog = targetPermanent.getCard().getName() + " is destroyed.";
                        gameBroadcastService.logAndBroadcast(gameData, destroyLog);
                        log.info("Game {} - {} is destroyed", gameData.id, targetPermanent.getCard().getName());
                        gameHelper.removeOrphanedAuras(gameData);
                    }
                }
            } else {
                String logEntry = cardName + "'s damage is prevented.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealOrderedDamageToAnyTargets(GameData gameData, StackEntry entry, DealOrderedDamageToAnyTargetsEffect effect) {
        List<UUID> targets = entry.getTargetPermanentIds();
        int damageMultiplier = gameQueryService.getDamageMultiplier(gameData);
        List<Integer> damages = effect.damageAmounts().stream().map(d -> d * damageMultiplier).toList();
        String cardName = entry.getCard().getName();

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = cardName + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        List<Permanent> destroyed = new ArrayList<>();

        for (int i = 0; i < Math.min(targets.size(), damages.size()); i++) {
            UUID targetId = targets.get(i);
            int damage = damages.get(i);

            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

            if (!targetIsPlayer && targetPermanent == null) {
                continue;
            }

            if (targetIsPlayer) {
                if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, targetId, entry.getCard().getColor())) {
                    int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, targetId, damage);
                    effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, targetId, effectiveDamage, cardName);
                    int currentLife = gameData.playerLifeTotals.getOrDefault(targetId, 20);
                    gameData.playerLifeTotals.put(targetId, currentLife - effectiveDamage);

                    if (effectiveDamage > 0) {
                        String playerName = gameData.playerIdToName.get(targetId);
                        String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    }
                }
            } else {
                if (!gameQueryService.hasProtectionFrom(gameData, targetPermanent, entry.getCard().getColor())) {
                    int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, targetPermanent, damage);
                    String logEntry = cardName + " deals " + effectiveDamage + " damage to " + targetPermanent.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} deals {} damage to {}", gameData.id, cardName, effectiveDamage, targetPermanent.getCard().getName());

                    if (effectiveDamage >= gameQueryService.getEffectiveToughness(gameData, targetPermanent)) {
                        if (gameQueryService.hasKeyword(gameData, targetPermanent, Keyword.INDESTRUCTIBLE)) {
                            String indestructibleLog = targetPermanent.getCard().getName() + " is indestructible and survives.";
                            gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
                        } else if (!gameHelper.tryRegenerate(gameData, targetPermanent)) {
                            destroyed.add(targetPermanent);
                        }
                    }
                } else {
                    String logEntry = cardName + "'s damage to " + targetPermanent.getCard().getName() + " is prevented.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        }

        for (Permanent target : destroyed) {
            gameHelper.removePermanentToGraveyard(gameData, target);
            String destroyLog = target.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, destroyLog);
            log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
        }

        if (!destroyed.isEmpty()) {
            gameHelper.removeOrphanedAuras(gameData);
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToAnyTargetAndGainLife(GameData gameData, StackEntry entry, DealDamageToAnyTargetAndGainLifeEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        int lifeGain = effect.lifeGain();
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();

        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

        if (!targetIsPlayer && targetPermanent == null) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = cardName + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (targetIsPlayer) {
            if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, targetId, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, targetId, damage);
                effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, targetId, effectiveDamage, cardName);
                int currentLife = gameData.playerLifeTotals.getOrDefault(targetId, 20);
                gameData.playerLifeTotals.put(targetId, currentLife - effectiveDamage);

                if (effectiveDamage > 0) {
                    String playerName = gameData.playerIdToName.get(targetId);
                    String logEntry = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        } else {
            if (!gameQueryService.hasProtectionFrom(gameData, targetPermanent, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, targetPermanent, damage);
                String logEntry = cardName + " deals " + effectiveDamage + " damage to " + targetPermanent.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} deals {} damage to {}", gameData.id, cardName, effectiveDamage, targetPermanent.getCard().getName());

                if (effectiveDamage >= gameQueryService.getEffectiveToughness(gameData, targetPermanent)) {
                    if (gameQueryService.hasKeyword(gameData, targetPermanent, Keyword.INDESTRUCTIBLE)) {
                        String indestructibleLog = targetPermanent.getCard().getName() + " is indestructible and survives.";
                        gameBroadcastService.logAndBroadcast(gameData, indestructibleLog);
                    } else if (!gameHelper.tryRegenerate(gameData, targetPermanent)) {
                        gameHelper.removePermanentToGraveyard(gameData, targetPermanent);
                        String destroyLog = targetPermanent.getCard().getName() + " is destroyed.";
                        gameBroadcastService.logAndBroadcast(gameData, destroyLog);
                        log.info("Game {} - {} is destroyed", gameData.id, targetPermanent.getCard().getName());
                        gameHelper.removeOrphanedAuras(gameData);
                    }
                }
            } else {
                String logEntry = cardName + "'s damage is prevented.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        // Life gain is independent of damage prevention — always happens if the spell resolves
        int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
        gameData.playerLifeTotals.put(controllerId, currentLife + lifeGain);
        String controllerName = gameData.playerIdToName.get(controllerId);
        String lifeLog = controllerName + " gains " + lifeGain + " life.";
        gameBroadcastService.logAndBroadcast(gameData, lifeLog);
        log.info("Game {} - {} gains {} life", gameData.id, controllerName, lifeGain);

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToController(GameData gameData, StackEntry entry, DealDamageToControllerEffect effect) {
        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logEntry = cardName + "'s damage to controller is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, controllerId, entry.getCard().getColor())) {
            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, controllerId, damage);
            effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, controllerId, effectiveDamage, cardName);
            int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
            gameData.playerLifeTotals.put(controllerId, currentLife - effectiveDamage);

            if (effectiveDamage > 0) {
                String controllerName = gameData.playerIdToName.get(controllerId);
                String logEntry = controllerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} takes {} damage from {}", gameData.id, controllerName, effectiveDamage, cardName);
            }
        }

        gameHelper.checkWinCondition(gameData);
    }
}
