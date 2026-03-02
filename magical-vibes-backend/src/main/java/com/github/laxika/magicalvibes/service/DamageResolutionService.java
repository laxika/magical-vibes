package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSourcePowerToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetControllerIfTargetHasKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect;
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
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
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
    private final LifeResolutionService lifeResolutionService;

    @HandlesEffect(DealXDamageToTargetCreatureEffect.class)
    void resolveDealXDamageToTargetCreature(GameData gameData, StackEntry entry) {
        resolveCreatureTargetDamage(gameData, entry, gameQueryService.applyDamageMultiplier(gameData, entry.getXValue()));
    }

    @HandlesEffect(DealDamageToTargetCreatureEffect.class)
    void resolveDealDamageToTargetCreature(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect effect) {
        resolveCreatureTargetDamage(gameData, entry, gameQueryService.applyDamageMultiplier(gameData, effect.damage()));
    }

    @HandlesEffect(DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect.class)
    void resolveDealDamageToTargetCreatureEqualToControlledSubtypeCount(
            GameData gameData,
            StackEntry entry,
            DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect
    ) {
        int count = gameQueryService.countControlledSubtypePermanents(gameData, entry.getControllerId(), effect.subtype());
        resolveCreatureTargetDamage(gameData, entry, gameQueryService.applyDamageMultiplier(gameData, count));
    }

    @HandlesEffect(DealXDamageDividedAmongTargetAttackingCreaturesEffect.class)
    void resolveDealXDamageDividedAmongTargetAttackingCreatures(GameData gameData, StackEntry entry) {
        Map<UUID, Integer> assignments = entry.getDamageAssignments();
        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        if (isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> destroyed = new ArrayList<>();

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
            if (target == null) continue;
            if (hasProtectionFromSource(gameData, target, entry)) continue;

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, assignment.getValue());
            if (dealCreatureDamage(gameData, entry, target, rawDamage)) {
                destroyed.add(target);
            }
        }

        destroyAllLethal(gameData, destroyed);
    }

    @HandlesEffect(MassDamageEffect.class)
    void resolveMassDamage(GameData gameData, StackEntry entry, MassDamageEffect effect) {
        if (isDamageSourcePreventedWithLog(gameData, entry)) return;

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
        lifeResolutionService.applyGainLife(gameData, entry.getControllerId(), xValue);

        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(DealDamageToTargetPlayerEffect.class)
    void resolveDealDamageToTargetPlayer(GameData gameData, StackEntry entry, DealDamageToTargetPlayerEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!isDamageSourcePreventedWithLog(gameData, entry)) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(DealDamageToTargetPlayerByHandSizeEffect.class)
    void resolveDealDamageToTargetPlayerByHandSize(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!isDamageSourcePreventedWithLog(gameData, entry)) {
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

        if (!isDamageSourcePreventedWithLog(gameData, entry)) {
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

    @HandlesEffect(DealDamageEqualToSourcePowerToAnyTargetEffect.class)
    void resolveDealDamageEqualToSourcePowerToAnyTarget(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) return;

        int power = gameQueryService.getEffectivePower(gameData, source);
        if (power <= 0) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power);
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(DealOrderedDamageToAnyTargetsEffect.class)
    void resolveDealOrderedDamageToAnyTargets(GameData gameData, StackEntry entry, DealOrderedDamageToAnyTargetsEffect effect) {
        List<UUID> targets = entry.getTargetPermanentIds();
        int damageMultiplier = gameQueryService.getDamageMultiplier(gameData);
        List<Integer> damages = effect.damageAmounts().stream().map(d -> d * damageMultiplier).toList();
        String cardName = entry.getCard().getName();

        if (isDamageSourcePreventedWithLog(gameData, entry)) return;

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
                if (!hasProtectionFromSource(gameData, targetPermanent, entry)) {
                    if (dealCreatureDamage(gameData, entry, targetPermanent, damage)) {
                        destroyed.add(targetPermanent);
                    }
                } else {
                    gameBroadcastService.logAndBroadcast(gameData,
                            cardName + "'s damage to " + targetPermanent.getCard().getName() + " is prevented.");
                }
            }
        }

        destroyAllLethal(gameData, destroyed);

        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(DealDamageToAnyTargetAndGainLifeEffect.class)
    void resolveDealDamageToAnyTargetAndGainLife(GameData gameData, StackEntry entry, DealDamageToAnyTargetAndGainLifeEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);

        // Life gain is independent of damage prevention — always happens if the spell resolves
        lifeResolutionService.applyGainLife(gameData, entry.getControllerId(), effect.lifeGain());

        gameHelper.checkWinCondition(gameData);
    }

    /**
     * Applies damage to a creature, handling prevention shield, recording, logging,
     * and checking for lethal damage (indestructible/regenerate).
     * Returns true if the creature took lethal damage and should be destroyed.
     * Caller is responsible for removal (use {@link #destroyPermanent} for single-target,
     * or batch-collect for multi-target effects).
     */
    private boolean dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        return dealCreatureDamage(gameData, entry, target, rawDamage, null);
    }

    /**
     * Overload that accepts an explicit damage source permanent (e.g. the biting creature).
     * When {@code damageSource} is non-null, its ID is used for recording, its name for logging,
     * and keywords are checked directly on it. When null, falls back to entry-based lookup.
     */
    private boolean dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage, Permanent damageSource) {
        int damage = gameHelper.applyCreaturePreventionShield(gameData, target, rawDamage);

        if (damageSource != null) {
            gameHelper.recordCreatureDamagedByPermanent(gameData, damageSource.getId(), target, damage);
        } else if (entry.getSourcePermanentId() != null) {
            gameHelper.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        }

        String sourceName = damageSource != null ? damageSource.getCard().getName() : entry.getCard().getName();

        boolean sourceHasInfect = sourceHasKeyword(gameData, entry, damageSource, Keyword.INFECT);

        if (sourceHasInfect) {
            if (damage > 0) {
                target.setMinusOneMinusOneCounters(target.getMinusOneMinusOneCounters() + damage);
                gameBroadcastService.logAndBroadcast(gameData,
                        sourceName + " puts " + damage + " -1/-1 counters on " + target.getCard().getName() + ".");
                log.info("Game {} - {} puts {} -1/-1 counters on {}", gameData.id, sourceName, damage, target.getCard().getName());
            }
            // CR 704.5f: 0 toughness from -1/-1 counters — dies regardless of indestructible
            return gameQueryService.getEffectiveToughness(gameData, target) <= 0;
        }

        gameBroadcastService.logAndBroadcast(gameData,
                sourceName + " deals " + damage + " damage to " + target.getCard().getName() + ".");
        log.info("Game {} - {} deals {} damage to {}", gameData.id, sourceName, damage, target.getCard().getName());

        boolean sourceHasDeathtouch = sourceHasKeyword(gameData, entry, damageSource, Keyword.DEATHTOUCH);
        boolean isLethal = damage >= gameQueryService.getEffectiveToughness(gameData, target)
                || (damage >= 1 && sourceHasDeathtouch);
        if (isLethal) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        target.getCard().getName() + " is indestructible and survives.");
                return false;
            }
            return !gameHelper.tryRegenerate(gameData, target);
        }
        return false;
    }

    private boolean hasKeywordOnSource(GameData gameData, StackEntry entry, Keyword keyword) {
        if (entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                return gameQueryService.hasKeyword(gameData, source, keyword);
            }
        }
        return false;
    }

    private boolean sourceHasKeyword(GameData gameData, StackEntry entry, Permanent damageSource, Keyword keyword) {
        return damageSource != null
                ? gameQueryService.hasKeyword(gameData, damageSource, keyword)
                : hasKeywordOnSource(gameData, entry, keyword);
    }

    private void destroyPermanent(GameData gameData, Permanent target) {
        permanentRemovalService.removePermanentToGraveyard(gameData, target);
        gameBroadcastService.logAndBroadcast(gameData, target.getCard().getName() + " is destroyed.");
        log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
    }

    private void destroyAllLethal(GameData gameData, List<Permanent> destroyed) {
        for (Permanent target : destroyed) {
            destroyPermanent(gameData, target);
        }
        if (!destroyed.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    private void dealDamageAndDestroyIfLethal(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        dealDamageAndDestroyIfLethal(gameData, entry, target, rawDamage, null);
    }

    private void dealDamageAndDestroyIfLethal(GameData gameData, StackEntry entry, Permanent target, int rawDamage, Permanent damageSource) {
        if (dealCreatureDamage(gameData, entry, target, rawDamage, damageSource)) {
            destroyPermanent(gameData, target);
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    private boolean isDamageSourcePreventedWithLog(GameData gameData, StackEntry entry) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, entry.getCard().getName() + "'s damage is prevented.");
            return true;
        }
        return false;
    }

    private void resolveCreatureTargetDamage(GameData gameData, StackEntry entry, int damage) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;
        if (isDamagePreventedForCreature(gameData, entry, target)) return;
        dealDamageAndDestroyIfLethal(gameData, entry, target, damage);
    }

    private boolean isDamagePreventedForCreature(GameData gameData, StackEntry entry, Permanent target) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())
                || hasProtectionFromSource(gameData, target, entry)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + "'s damage is prevented.");
            return true;
        }
        return false;
    }

    private boolean hasProtectionFromSource(GameData gameData, Permanent target, StackEntry entry) {
        return gameQueryService.hasProtectionFrom(gameData, target, entry.getCard().getColor())
                || gameQueryService.hasProtectionFromSourceCardTypes(target, entry.getCard());
    }

    private boolean isSourcePermanentPreventedFromDealingDamage(GameData gameData, StackEntry entry) {
        return entry.getSourcePermanentId() != null
                && gameData.permanentsPreventedFromDealingDamage.contains(entry.getSourcePermanentId());
    }

    private void resolveAnyTargetDamage(GameData gameData, StackEntry entry, UUID targetId, int rawDamage, boolean cantRegenerate) {
        String cardName = entry.getCard().getName();
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

        if (!targetIsPlayer && targetPermanent == null) return;

        if (isDamageSourcePreventedWithLog(gameData, entry)) return;

        if (targetIsPlayer) {
            // dealDamageToPlayer handles per-permanent prevention (permanentsPreventedFromDealingDamage)
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        } else {
            if (isSourcePermanentPreventedFromDealingDamage(gameData, entry)
                    || hasProtectionFromSource(gameData, targetPermanent, entry)) {
                gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage is prevented.");
                return;
            }
            if (cantRegenerate) {
                targetPermanent.setCantRegenerateThisTurn(true);
            }
            dealDamageAndDestroyIfLethal(gameData, entry, targetPermanent, rawDamage);
        }
    }

    private void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, int damage, Predicate<Permanent> filter) {
        List<Permanent> destroyed = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                destroyed.addAll(damageFilteredCreatures(gameData, entry, damage, battlefield, filter))
        );
        destroyAllLethal(gameData, destroyed);
    }

    private List<Permanent> damageFilteredCreatures(GameData gameData, StackEntry entry, int damage, Collection<Permanent> permanents, Predicate<Permanent> filter) {
        List<Permanent> destroyed = new ArrayList<>();
        for (Permanent p : permanents) {
            if (!filter.test(p)) continue;
            if (hasProtectionFromSource(gameData, p, entry)) continue;
            if (dealCreatureDamage(gameData, entry, p, damage)) {
                destroyed.add(p);
            }
        }
        return destroyed;
    }

    private void dealDamageToPlayer(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        String cardName = entry.getCard().getName();
        if (gameHelper.isSourceDamagePreventedForPlayer(gameData, playerId, entry.getSourcePermanentId())
                || isSourcePermanentPreventedFromDealingDamage(gameData, entry)) {
            gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            return;
        }
        if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, playerId, entry.getCard().getColor())) {
            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, playerId, rawDamage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);

            boolean sourceHasInfect = hasKeywordOnSource(gameData, entry, Keyword.INFECT);

            if (sourceHasInfect) {
                if (effectiveDamage > 0) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                    gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameBroadcastService.logAndBroadcast(gameData,
                            playerName + " gets " + effectiveDamage + " poison counters from " + cardName + ".");
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + "'s life total can't change.");
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
                triggerCollectionService.checkDamageDealtToControllerTriggers(gameData, playerId, entry.getSourcePermanentId(), false);
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
        CardColor biterColor = biter.getEffectiveColor();
        if (gameQueryService.hasProtectionFrom(gameData, target, biterColor)
                || gameQueryService.hasProtectionFromSourceCardTypes(gameData, target, biter)) {
            String logEntry = target.getCard().getName() + " has protection from " + (biterColor != null ? biterColor.name().toLowerCase() : "source") + " — damage prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        int power = gameQueryService.getEffectivePower(gameData, biter);
        if (power <= 0) {
            return;
        }

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, power);
        dealDamageAndDestroyIfLethal(gameData, entry, target, rawDamage, biter);
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
                    Predicate<Permanent> creatureFilter = p -> gameQueryService.isCreature(gameData, p);
                    destroyAllLethal(gameData, damageFilteredCreatures(gameData, entry, damage, battlefield, creatureFilter));
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

    @HandlesEffect(DealDamageToTargetControllerIfTargetHasKeywordEffect.class)
    void resolveDealDamageToTargetControllerIfTargetHasKeyword(
            GameData gameData,
            StackEntry entry,
            DealDamageToTargetControllerIfTargetHasKeywordEffect effect
    ) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        if (!gameQueryService.hasKeyword(gameData, target, effect.keyword())) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        String cardName = entry.getCard().getName();

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + "'s damage to " + gameData.playerIdToName.get(controllerId) + " is prevented.");
        } else {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
            dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
        }

        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect.class)
    void resolveDealDamageToEachOpponentEqualToCardsDrawnThisTurn(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;

            int cardsDrawn = gameData.cardsDrawnThisTurn.getOrDefault(playerId, 0);
            if (cardsDrawn <= 0) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " has drawn no cards this turn — no damage from " + cardName + ".");
                log.info("Game {} - {} drawn 0 cards this turn, no damage from {}",
                        gameData.id, playerName, cardName);
                continue;
            }

            if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        cardName + "'s damage to " + playerName + " is prevented.");
            } else {
                int rawDamage = gameQueryService.applyDamageMultiplier(gameData, cardsDrawn);
                dealDamageToPlayer(gameData, entry, playerId, rawDamage);
            }
        }

        gameHelper.checkWinCondition(gameData);
    }
}

