package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardDealManaValueDamageEffect;
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
public class DamageResolutionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    @HandlesEffect(DealXDamageToTargetCreatureEffect.class)
    void resolveDealXDamageToTargetCreature(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;
        if (isDamagePreventedForCreature(gameData, entry, target)) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue());
        if (dealCreatureDamage(gameData, entry, target, rawDamage)) {
            destroyPermanent(gameData, target);
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    @HandlesEffect(DealDamageToTargetCreatureEffect.class)
    void resolveDealDamageToTargetCreature(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;
        if (isDamagePreventedForCreature(gameData, entry, target)) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        if (dealCreatureDamage(gameData, entry, target, rawDamage)) {
            destroyPermanent(gameData, target);
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    @HandlesEffect(DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect.class)
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
            permanentRemovalService.removeOrphanedAuras(gameData);
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

    @HandlesEffect(DealXDamageDividedAmongTargetAttackingCreaturesEffect.class)
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
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    @HandlesEffect(MassDamageEffect.class)
    void resolveMassDamage(GameData gameData, StackEntry entry, MassDamageEffect effect) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + "'s damage is prevented.");
            return;
        }

        int baseDamage = effect.usesXValue() ? entry.getXValue() : effect.damage();
        int damage = gameQueryService.applyDamageMultiplier(gameData, baseDamage);

        Predicate<Permanent> creatureFilter = effect.filter() == null
                ? p -> gameQueryService.isCreature(gameData, p)
                : p -> gameQueryService.isCreature(gameData, p)
                        && gameQueryService.matchesPermanentPredicate(gameData, p, effect.filter());

        damageAllCreaturesOnBattlefield(gameData, entry, damage, creatureFilter);

        if (effect.damagesPlayers()) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                dealDamageToPlayer(gameData, entry, playerId, damage);
            }
            gameHelper.checkWinCondition(gameData);
        }
    }

    @HandlesEffect(DealXDamageToAnyTargetEffect.class)
    void resolveDealXDamageToAnyTarget(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue());
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(DealXDamageToAnyTargetAndGainXLifeEffect.class)
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

    @HandlesEffect(DealDamageToTargetPlayerEffect.class)
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

    @HandlesEffect(DealDamageToTargetPlayerByHandSizeEffect.class)
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

    @HandlesEffect(DealDamageIfFewCardsInHandEffect.class)
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

    @HandlesEffect(DealDamageToAnyTargetEffect.class)
    void resolveDealDamageToAnyTarget(GameData gameData, StackEntry entry, DealDamageToAnyTargetEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, effect.cantRegenerate());
        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(DealOrderedDamageToAnyTargetsEffect.class)
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
            permanentRemovalService.removeOrphanedAuras(gameData);
        }

        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(DealDamageToAnyTargetAndGainLifeEffect.class)
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

        // Check if source permanent has infect
        boolean sourceHasInfect = hasInfectSource(gameData, entry);

        if (sourceHasInfect) {
            if (damage > 0) {
                target.setMinusOneMinusOneCounters(target.getMinusOneMinusOneCounters() + damage);
                gameBroadcastService.logAndBroadcast(gameData,
                        cardName + " puts " + damage + " -1/-1 counters on " + target.getCard().getName() + ".");
                log.info("Game {} - {} puts {} -1/-1 counters on {}", gameData.id, cardName, damage, target.getCard().getName());
            }
            // CR 704.5f: 0 toughness from -1/-1 counters — dies regardless of indestructible
            return gameQueryService.getEffectiveToughness(gameData, target) <= 0;
        }

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

    private boolean hasInfectSource(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                return gameQueryService.hasKeyword(gameData, source, Keyword.INFECT);
            }
        }
        return false;
    }

    private void destroyPermanent(GameData gameData, Permanent target) {
        permanentRemovalService.removePermanentToGraveyard(gameData, target);
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
                    permanentRemovalService.removeOrphanedAuras(gameData);
                }
            } else {
                gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage is prevented.");
            }
        }
    }

    private void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, int damage, Predicate<Permanent> filter) {
        String cardName = entry.getCard().getName();
        boolean sourceHasInfect = hasInfectSource(gameData, entry);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            Set<Integer> deadIndices = new TreeSet<>(Collections.reverseOrder());
            for (int i = 0; i < battlefield.size(); i++) {
                Permanent p = battlefield.get(i);
                if (!filter.test(p)) continue;
                if (gameQueryService.hasProtectionFrom(gameData, p, entry.getCard().getColor())) continue;

                int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, p, damage);
                recordCreatureDamageFromPermanentSource(gameData, entry, p, effectiveDamage);

                if (sourceHasInfect) {
                    if (effectiveDamage > 0) {
                        p.setMinusOneMinusOneCounters(p.getMinusOneMinusOneCounters() + effectiveDamage);
                    }
                    if (gameQueryService.getEffectiveToughness(gameData, p) <= 0) {
                        deadIndices.add(i);
                    }
                } else {
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
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + "'s " + dead.getCard().getName() + " is destroyed by " + cardName + ".");
                gameHelper.addCardToGraveyard(gameData, playerId, dead.getOriginalCard(), Zone.BATTLEFIELD);
                gameHelper.collectDeathTrigger(gameData, dead.getCard(), playerId, true);
                gameHelper.checkAllyCreatureDeathTriggers(gameData, playerId);
                battlefield.remove(idx);
            }
        });

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void dealDamageToPlayer(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        String cardName = entry.getCard().getName();
        if (gameHelper.isSourceDamagePreventedForPlayer(gameData, playerId, entry.getSourcePermanentId())) {
            gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            return;
        }
        if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, playerId, entry.getCard().getColor())) {
            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, playerId, rawDamage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);

            boolean sourceHasInfect = hasInfectSource(gameData, entry);

            if (sourceHasInfect) {
                if (effectiveDamage > 0) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                    gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameBroadcastService.logAndBroadcast(gameData,
                            playerName + " gets " + effectiveDamage + " poison counters from " + cardName + ".");
                }
            } else {
                int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
                gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

                if (effectiveDamage > 0) {
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameBroadcastService.logAndBroadcast(gameData,
                            playerName + " takes " + effectiveDamage + " damage from " + cardName + ".");
                }
            }

            if (effectiveDamage > 0) {
                triggerCollectionService.checkDamageDealtToControllerTriggers(gameData, playerId, entry.getSourcePermanentId());
            }
        }
    }

    @HandlesEffect(FirstTargetDealsPowerDamageToSecondTargetEffect.class)
    void resolveBite(GameData gameData, StackEntry entry) {
        List<UUID> targets = entry.getTargetPermanentIds();
        if (targets == null || targets.size() < 2) {
            return; // No second target — "up to one" chose zero
        }

        UUID biterId = targets.get(0);
        UUID targetId = targets.get(1);

        Permanent biter = gameQueryService.findPermanentById(gameData, biterId);
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (biter == null || target == null) {
            return;
        }

        // The biting creature deals the damage — check if it is prevented from dealing damage
        if (gameQueryService.isPreventedFromDealingDamage(gameData, biter)) {
            String logEntry = biter.getCard().getName() + "'s damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        // Use the biting creature's color for protection checks (not the spell's color)
        CardColor biterColor = biter.getCard().getColor();
        if (gameQueryService.hasProtectionFrom(gameData, target, biterColor)) {
            String logEntry = target.getCard().getName() + " has protection from " + biterColor.name().toLowerCase() + " — damage prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int power = gameQueryService.getEffectivePower(gameData, biter);
        if (power <= 0) {
            return;
        }

        int damage = gameHelper.applyCreaturePreventionShield(gameData, target, gameQueryService.applyDamageMultiplier(gameData, power));
        gameHelper.recordCreatureDamagedByPermanent(gameData, biter.getId(), target, damage);
        String logEntry = biter.getCard().getName() + " deals " + damage + " damage to " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} bites {} for {} damage", gameData.id, biter.getCard().getName(), target.getCard().getName(), damage);

        if (damage >= gameQueryService.getEffectiveToughness(gameData, target)) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        target.getCard().getName() + " is indestructible and survives.");
            } else if (!gameHelper.tryRegenerate(gameData, target)) {
                destroyPermanent(gameData, target);
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }
    }

    @HandlesEffect(RevealTopCardDealManaValueDamageEffect.class)
    void resolveRevealTopCardDealManaValueDamage(GameData gameData, StackEntry entry, RevealTopCardDealManaValueDamageEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (!gameData.playerIds.contains(targetPlayerId)) return;

        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String cardName = entry.getCard().getName();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, targetPlayerName + "'s library is empty.");
            return;
        }

        Card topCard = deck.getFirst();
        int manaValue = topCard.getManaValue();
        gameBroadcastService.logAndBroadcast(gameData,
                targetPlayerName + " reveals " + topCard.getName() + " (mana value " + manaValue + ") from the top of their library.");

        if (manaValue > 0 && !gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue);

            if (effect.damageTargetPlayer()) {
                dealDamageToPlayer(gameData, entry, targetPlayerId, damage);
            }

            if (effect.damageTargetCreatures()) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
                if (battlefield != null) {
                    List<Permanent> destroyed = new ArrayList<>();
                    for (Permanent p : battlefield) {
                        if (!gameQueryService.isCreature(gameData, p)) continue;
                        if (gameQueryService.hasProtectionFrom(gameData, p, entry.getCard().getColor())) continue;

                        int effectiveDamage = gameHelper.applyCreaturePreventionShield(gameData, p, damage);
                        gameBroadcastService.logAndBroadcast(gameData,
                                cardName + " deals " + effectiveDamage + " damage to " + p.getCard().getName() + ".");

                        if (effectiveDamage >= gameQueryService.getEffectiveToughness(gameData, p)
                                && !gameQueryService.hasKeyword(gameData, p, Keyword.INDESTRUCTIBLE)
                                && !gameHelper.tryRegenerate(gameData, p)) {
                            destroyed.add(p);
                        }
                    }

                    for (Permanent dead : destroyed) {
                        destroyPermanent(gameData, dead);
                    }
                    if (!destroyed.isEmpty()) {
                        permanentRemovalService.removeOrphanedAuras(gameData);
                    }
                }
            }

            gameHelper.checkWinCondition(gameData);
        }

        if (effect.returnToHandIfLand() && topCard.getType() == CardType.LAND) {
            gameBroadcastService.logAndBroadcast(gameData,
                    "A land card was revealed — " + cardName + " is returned to its owner's hand.");
            entry.setReturnToHandAfterResolving(true);
        }
    }

    @HandlesEffect(DealDamageToControllerEffect.class)
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

