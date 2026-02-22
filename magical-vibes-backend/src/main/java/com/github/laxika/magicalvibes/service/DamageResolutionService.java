package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesAndPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToFlyingAndPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

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
        registry.register(DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect.class,
                (gd, entry, effect) -> resolveDealDamageToTargetCreatureEqualToControlledSubtypeCount(
                        gd, entry, (DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect) effect));
        registry.register(DealXDamageDividedAmongTargetAttackingCreaturesEffect.class,
                (gd, entry, effect) -> resolveDealXDamageDividedAmongTargetAttackingCreatures(gd, entry));
        registry.register(DealDamageToAllCreaturesEffect.class,
                (gd, entry, effect) -> resolveDealDamageToAllCreatures(gd, entry, (DealDamageToAllCreaturesEffect) effect));
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
        registry.register(DealDamageToTargetPlayerByHandSizeEffect.class,
                (gd, entry, effect) -> resolveDealDamageToTargetPlayerByHandSize(gd, entry));
        registry.register(DealOrderedDamageToAnyTargetsEffect.class,
                (gd, entry, effect) -> resolveDealOrderedDamageToAnyTargets(gd, entry, (DealOrderedDamageToAnyTargetsEffect) effect));
        registry.register(DealDamageIfFewCardsInHandEffect.class,
                (gd, entry, effect) -> resolveDealDamageIfFewCardsInHand(gd, entry, (DealDamageIfFewCardsInHandEffect) effect));
    }

    void resolveDealXDamageToTargetCreature(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;
        if (isDamagePreventedForCreature(gameData, entry, target)) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue());
        if (dealCreatureDamage(gameData, entry, target, rawDamage)) {
            destroyPermanent(gameData, target);
            gameHelper.removeOrphanedAuras(gameData);
        }
    }

    void resolveDealDamageToTargetCreature(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;
        if (isDamagePreventedForCreature(gameData, entry, target)) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        if (dealCreatureDamage(gameData, entry, target, rawDamage)) {
            destroyPermanent(gameData, target);
            gameHelper.removeOrphanedAuras(gameData);
        }
    }

    void resolveDealDamageToTargetCreatureEqualToControlledSubtypeCount(
            GameData gameData,
            StackEntry entry,
            DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect
    ) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;
        if (isDamagePreventedForCreature(gameData, entry, target)) return;

        int controlledSubtypeCount = countControlledSubtypePermanents(gameData, entry.getControllerId(), effect.subtype());
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, controlledSubtypeCount);
        if (dealCreatureDamage(gameData, entry, target, rawDamage)) {
            destroyPermanent(gameData, target);
            gameHelper.removeOrphanedAuras(gameData);
        }
    }

    private int countControlledSubtypePermanents(GameData gameData, UUID controllerId, CardSubtype subtype) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getSubtypes().contains(subtype)) {
                count++;
            }
        }
        return count;
    }

    void resolveDealXDamageDividedAmongTargetAttackingCreatures(GameData gameData, StackEntry entry) {
        Map<UUID, Integer> assignments = entry.getDamageAssignments();
        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + "'s damage is prevented.");
            return;
        }

        List<Permanent> destroyed = new ArrayList<>();

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
            if (target == null) continue;
            if (gameQueryService.hasProtectionFrom(gameData, target, entry.getCard().getColor())) continue;

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, assignment.getValue());
            if (dealCreatureDamage(gameData, entry, target, rawDamage)) {
                destroyed.add(target);
            }
        }

        for (Permanent target : destroyed) {
            destroyPermanent(gameData, target);
        }
        if (!destroyed.isEmpty()) {
            gameHelper.removeOrphanedAuras(gameData);
        }
    }

    void resolveDealDamageToAllCreatures(GameData gameData, StackEntry entry, DealDamageToAllCreaturesEffect effect) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + "'s damage is prevented.");
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        damageAllCreaturesOnBattlefield(gameData, entry, damage, p -> gameQueryService.isCreature(gameData, p));
    }

    void resolveDealDamageToAllCreaturesAndPlayers(GameData gameData, StackEntry entry, DealDamageToAllCreaturesAndPlayersEffect effect) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + "'s damage is prevented.");
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        damageAllCreaturesOnBattlefield(gameData, entry, damage, p -> gameQueryService.isCreature(gameData, p));

        for (UUID playerId : gameData.orderedPlayerIds) {
            dealDamageToPlayer(gameData, entry, playerId, damage);
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToFlyingAndPlayers(GameData gameData, StackEntry entry) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + "'s damage is prevented.");
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue());
        damageAllCreaturesOnBattlefield(gameData, entry, damage, p -> gameQueryService.hasKeyword(gameData, p, Keyword.FLYING));

        for (UUID playerId : gameData.orderedPlayerIds) {
            dealDamageToPlayer(gameData, entry, playerId, damage);
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealXDamageToAnyTarget(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue());
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealXDamageToAnyTargetAndGainXLife(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int xValue = entry.getXValue();
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, xValue);
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);

        // Life gain is independent of damage prevention — always happens if the spell resolves
        UUID controllerId = entry.getControllerId();
        int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
        gameData.playerLifeTotals.put(controllerId, currentLife + xValue);
        String controllerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, controllerName + " gains " + xValue + " life.");
        log.info("Game {} - {} gains {} life", gameData.id, controllerName, xValue);

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToTargetPlayer(GameData gameData, StackEntry entry, DealDamageToTargetPlayerEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + "'s damage is prevented.");
        } else {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToTargetPlayerByHandSize(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + "'s damage is prevented.");
        } else {
            List<Card> hand = gameData.playerHands.get(targetId);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, hand != null ? hand.size() : 0);
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageIfFewCardsInHand(GameData gameData, StackEntry entry, DealDamageIfFewCardsInHandEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        String cardName = entry.getCard().getName();

        if (!gameData.playerIds.contains(targetId)) return;

        // Intervening-if: re-check condition at resolution time
        List<Card> hand = gameData.playerHands.get(targetId);
        int handSize = hand != null ? hand.size() : 0;
        if (handSize > effect.maxCards()) {
            String playerName = gameData.playerIdToName.get(targetId);
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + "'s ability does nothing — " + playerName + " has more than " + effect.maxCards() + " cards in hand.");
            return;
        }

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage is prevented.");
        } else {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToAnyTarget(GameData gameData, StackEntry entry, DealDamageToAnyTargetEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, effect.cantRegenerate());
        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealOrderedDamageToAnyTargets(GameData gameData, StackEntry entry, DealOrderedDamageToAnyTargetsEffect effect) {
        List<UUID> targets = entry.getTargetPermanentIds();
        int damageMultiplier = gameQueryService.getDamageMultiplier(gameData);
        List<Integer> damages = effect.damageAmounts().stream().map(d -> d * damageMultiplier).toList();
        String cardName = entry.getCard().getName();

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage is prevented.");
            return;
        }

        List<Permanent> destroyed = new ArrayList<>();

        for (int i = 0; i < Math.min(targets.size(), damages.size()); i++) {
            UUID targetId = targets.get(i);
            int damage = damages.get(i);

            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

            if (!targetIsPlayer && targetPermanent == null) continue;

            if (targetIsPlayer) {
                dealDamageToPlayer(gameData, entry, targetId, damage);
            } else {
                if (!gameQueryService.hasProtectionFrom(gameData, targetPermanent, entry.getCard().getColor())) {
                    if (dealCreatureDamage(gameData, entry, targetPermanent, damage)) {
                        destroyed.add(targetPermanent);
                    }
                } else {
                    gameBroadcastService.logAndBroadcast(gameData,
                            cardName + "'s damage to " + targetPermanent.getCard().getName() + " is prevented.");
                }
            }
        }

        for (Permanent target : destroyed) {
            destroyPermanent(gameData, target);
        }
        if (!destroyed.isEmpty()) {
            gameHelper.removeOrphanedAuras(gameData);
        }

        gameHelper.checkWinCondition(gameData);
    }

    void resolveDealDamageToAnyTargetAndGainLife(GameData gameData, StackEntry entry, DealDamageToAnyTargetAndGainLifeEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);

        // Life gain is independent of damage prevention — always happens if the spell resolves
        int lifeGain = effect.lifeGain();
        UUID controllerId = entry.getControllerId();
        int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
        gameData.playerLifeTotals.put(controllerId, currentLife + lifeGain);
        String controllerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, controllerName + " gains " + lifeGain + " life.");
        log.info("Game {} - {} gains {} life", gameData.id, controllerName, lifeGain);

        gameHelper.checkWinCondition(gameData);
    }

    private void recordCreatureDamageFromPermanentSource(GameData gameData, StackEntry entry, Permanent targetCreature, int damage) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        gameHelper.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), targetCreature, damage);
    }

    /**
     * Applies damage to a creature, handling prevention shield, recording, logging,
     * and checking for lethal damage (indestructible/regenerate).
     * Returns true if the creature took lethal damage and should be destroyed.
     * Caller is responsible for removal (use {@link #destroyPermanent} for single-target,
     * or batch-collect for multi-target effects).
     */
    private boolean dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        int damage = gameHelper.applyCreaturePreventionShield(gameData, target, rawDamage);
        recordCreatureDamageFromPermanentSource(gameData, entry, target, damage);
        String cardName = entry.getCard().getName();

        gameBroadcastService.logAndBroadcast(gameData,
                cardName + " deals " + damage + " damage to " + target.getCard().getName() + ".");
        log.info("Game {} - {} deals {} damage to {}", gameData.id, cardName, damage, target.getCard().getName());

        if (damage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        target.getCard().getName() + " is indestructible and survives.");
                return false;
            }
            return !gameHelper.tryRegenerate(gameData, target);
        }
        return false;
    }

    private void destroyPermanent(GameData gameData, Permanent target) {
        gameHelper.removePermanentToGraveyard(gameData, target);
        gameBroadcastService.logAndBroadcast(gameData, target.getCard().getName() + " is destroyed.");
        log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
    }

    private boolean isDamagePreventedForCreature(GameData gameData, StackEntry entry, Permanent target) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())
                || gameQueryService.hasProtectionFrom(gameData, target, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + "'s damage is prevented.");
            return true;
        }
        return false;
    }

    private void resolveAnyTargetDamage(GameData gameData, StackEntry entry, UUID targetId, int rawDamage, boolean cantRegenerate) {
        String cardName = entry.getCard().getName();
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

        if (!targetIsPlayer && targetPermanent == null) return;

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage is prevented.");
        } else if (targetIsPlayer) {
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        } else {
            if (!gameQueryService.hasProtectionFrom(gameData, targetPermanent, entry.getCard().getColor())) {
                if (cantRegenerate) {
                    targetPermanent.setCantRegenerateThisTurn(true);
                }
                if (dealCreatureDamage(gameData, entry, targetPermanent, rawDamage)) {
                    destroyPermanent(gameData, targetPermanent);
                    gameHelper.removeOrphanedAuras(gameData);
                }
            } else {
                gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage is prevented.");
            }
        }
    }

    private void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, int damage, Predicate<Permanent> filter) {
        String cardName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            Set<Integer> deadIndices = new TreeSet<>(Collections.reverseOrder());
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent p = battlefield.get(i);
                if (!filter.test(p)) continue;
                if (gameQueryService.hasProtectionFrom(gameData, p, entry.getCard().getColor())) continue;

                int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, p, damage);
                recordCreatureDamageFromPermanentSource(gameData, entry, p, effectiveDamage);
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
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + "'s " + dead.getCard().getName() + " is destroyed by " + cardName + ".");
                gameHelper.addCardToGraveyard(gameData, playerId, dead.getOriginalCard(), Zone.BATTLEFIELD);
                gameHelper.collectDeathTrigger(gameData, dead.getCard(), playerId, true);
                gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                battlefield.remove(idx);
            }
        }

        gameHelper.removeOrphanedAuras(gameData);
    }

    private void dealDamageToPlayer(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        String cardName = entry.getCard().getName();
        if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, playerId, entry.getCard().getColor())) {
            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, playerId, rawDamage);
            effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);
            int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
            gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

            if (effectiveDamage > 0) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " takes " + effectiveDamage + " damage from " + cardName + ".");
            }
        }
    }

    void resolveDealDamageToController(GameData gameData, StackEntry entry, DealDamageToControllerEffect effect) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + "'s damage to controller is prevented.");
        } else {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
            dealDamageToPlayer(gameData, entry, entry.getControllerId(), rawDamage);
        }

        gameHelper.checkWinCondition(gameData);
    }
}

