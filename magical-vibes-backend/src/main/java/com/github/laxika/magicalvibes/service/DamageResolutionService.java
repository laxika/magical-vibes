package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageToFlyingAndPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
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
        registry.register(DealDamageToFlyingAndPlayersEffect.class,
                (gd, entry, effect) -> resolveDealDamageToFlyingAndPlayers(gd, entry));
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

        int damage = gameHelper.applyCreaturePreventionShield(gameData, target, entry.getXValue());
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

        int damage = gameHelper.applyCreaturePreventionShield(gameData, target, effect.damage());
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

            int damage = gameHelper.applyCreaturePreventionShield(gameData, target, assignment.getValue());
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

    void resolveDealDamageToFlyingAndPlayers(GameData gameData, StackEntry entry) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            String logMsg = entry.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        int damage = entry.getXValue();
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

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            for (int idx : deadIndices) {
                String playerName = gameData.playerIdToName.get(playerId);
                Permanent dead = battlefield.get(idx);
                String logEntry = playerName + "'s " + dead.getCard().getName() + " is destroyed by Hurricane.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                graveyard.add(dead.getOriginalCard());
                gameHelper.collectDeathTrigger(gameData, dead.getCard(), playerId, true);
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
}
