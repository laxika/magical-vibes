package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSourcePowerToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToItsOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetControllerIfTargetHasKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardDealManaValueDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDealDamageToDamagedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedEvenlyAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

/**
 * Resolves all damage-dealing effects dispatched from the stack.
 *
 * <p>Each {@code resolve*} method corresponds to a specific {@link com.github.laxika.magicalvibes.model.effect.CardEffect}
 * subtype and is discovered at runtime via the {@link HandlesEffect} annotation. The service handles damage
 * multipliers, prevention shields, protection checks, infect, deathtouch, indestructible, regeneration,
 * and lethal-damage destruction for both creature and player targets.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamageResolutionService {

    private final GraveyardService graveyardService;
    private final DamagePreventionService damagePreventionService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final LifeResolutionService lifeResolutionService;

    /**
     * Resolves {@link DealXDamageToTargetCreatureEffect} — deals X damage to the targeted creature.
     */
    @HandlesEffect(DealXDamageToTargetCreatureEffect.class)
    void resolveDealXDamageToTargetCreature(GameData gameData, StackEntry entry) {
        resolveCreatureTargetDamage(gameData, entry, gameQueryService.applyDamageMultiplier(gameData, entry.getXValue()));
    }

    /**
     * Resolves {@link DealDamageToTargetCreatureEffect} — deals a fixed amount of damage to the targeted creature.
     */
    @HandlesEffect(DealDamageToTargetCreatureEffect.class)
    void resolveDealDamageToTargetCreature(GameData gameData, StackEntry entry, DealDamageToTargetCreatureEffect effect) {
        resolveCreatureTargetDamage(gameData, entry, gameQueryService.applyDamageMultiplier(gameData, effect.damage()));
    }

    /**
     * Resolves {@link DealDamageToBlockedAttackersOnDeathEffect} — deals a fixed amount of damage to each
     * creature that was blocked by the dying creature. Target permanent IDs are baked in at trigger time.
     */
    @HandlesEffect(DealDamageToBlockedAttackersOnDeathEffect.class)
    void resolveDealDamageToBlockedAttackers(GameData gameData, StackEntry entry, DealDamageToBlockedAttackersOnDeathEffect effect) {
        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        if (isDamageSourcePreventedWithLog(gameData, entry)) return;
        for (UUID targetId : entry.getTargetPermanentIds()) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;
            if (!isDamagePreventedForCreature(gameData, entry, target)) {
                dealDamageAndDestroyIfLethal(gameData, entry, target, damage);
            }
        }
    }

    /**
     * Resolves {@link DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect} — deals damage to the
     * targeted creature equal to the number of permanents the controller has of a specific subtype.
     */
    @HandlesEffect(DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect.class)
    void resolveDealDamageToTargetCreatureEqualToControlledSubtypeCount(
            GameData gameData,
            StackEntry entry,
            DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect
    ) {
        int count = gameQueryService.countControlledSubtypePermanents(gameData, entry.getControllerId(), effect.subtype());
        resolveCreatureTargetDamage(gameData, entry, gameQueryService.applyDamageMultiplier(gameData, count));
    }

    /**
     * Resolves {@link DealXDamageDividedAmongTargetAttackingCreaturesEffect} — distributes X damage among
     * targeted attacking creatures according to the player's damage assignments.
     */
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
            if (gameQueryService.hasProtectionFromSource(gameData, target, entry.getCard())) continue;

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, assignment.getValue());
            if (dealCreatureDamage(gameData, entry, target, rawDamage)) {
                destroyed.add(target);
            }
        }

        destroyAllLethal(gameData, destroyed);
    }

    /**
     * Resolves {@link MassDamageEffect} — deals damage to all creatures on the battlefield (optionally
     * filtered by a predicate) and, if the effect specifies it, to all players as well.
     */
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
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    /**
     * Resolves {@link DealDamageToEachPlayerEffect} — deals a fixed amount of damage to each player (not creatures).
     */
    @HandlesEffect(DealDamageToEachPlayerEffect.class)
    void resolveDealDamageToEachPlayer(GameData gameData, StackEntry entry, DealDamageToEachPlayerEffect effect) {
        if (isDamageSourcePreventedWithLog(gameData, entry)) return;

        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        for (UUID playerId : gameData.orderedPlayerIds) {
            dealDamageToPlayer(gameData, entry, playerId, damage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealXDamageToAnyTargetEffect} — deals X damage to any target (creature or player).
     * When {@code exileInsteadOfDie} is true, marks the target creature for exile-instead-of-die this turn.
     */
    @HandlesEffect(DealXDamageToAnyTargetEffect.class)
    void resolveDealXDamageToAnyTarget(GameData gameData, StackEntry entry, DealXDamageToAnyTargetEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        // Mark the target creature for exile-instead-of-die before dealing damage,
        // so that if lethal damage destroys it immediately, the replacement applies.
        if (effect.exileInsteadOfDie()) {
            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            if (!targetIsPlayer) {
                Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
                if (targetPermanent != null && gameQueryService.isCreature(gameData, targetPermanent)) {
                    targetPermanent.setExileInsteadOfDieThisTurn(true);
                }
            }
        }

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue());
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealXDamageDividedEvenlyAmongTargetsEffect} — deals X damage divided evenly
     * (rounded down) among any number of targets (creatures and/or players).
     */
    @HandlesEffect(DealXDamageDividedEvenlyAmongTargetsEffect.class)
    void resolveDealXDamageDividedEvenlyAmongTargets(GameData gameData, StackEntry entry) {
        List<UUID> targets = entry.getTargetPermanentIds();
        if (targets.isEmpty()) {
            // Fall back to single target
            if (entry.getTargetPermanentId() != null) {
                targets = List.of(entry.getTargetPermanentId());
            } else {
                return;
            }
        }

        int x = entry.getXValue();
        int damagePerTarget = x / targets.size();
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damagePerTarget);
        String cardName = entry.getCard().getName();

        if (isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> destroyed = new ArrayList<>();

        for (UUID targetId : targets) {
            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

            if (!targetIsPlayer && targetPermanent == null) continue;

            if (targetIsPlayer) {
                dealDamageToPlayer(gameData, entry, targetId, rawDamage);
            } else {
                if (!gameQueryService.hasProtectionFromSource(gameData, targetPermanent, entry.getCard())) {
                    if (dealCreatureDamage(gameData, entry, targetPermanent, rawDamage)) {
                        destroyed.add(targetPermanent);
                    }
                } else {
                    gameBroadcastService.logAndBroadcast(gameData,
                            cardName + "'s damage to " + targetPermanent.getCard().getName() + " is prevented.");
                }
            }
        }

        destroyAllLethal(gameData, destroyed);
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealXDamageToAnyTargetAndGainXLifeEffect} — deals X damage to any target and
     * the controller gains X life. The life gain occurs regardless of damage prevention.
     */
    @HandlesEffect(DealXDamageToAnyTargetAndGainXLifeEffect.class)
    void resolveDealXDamageToAnyTargetAndGainXLife(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int xValue = entry.getXValue();
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, xValue);
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);

        // Life gain is independent of damage prevention — always happens if the spell resolves
        lifeResolutionService.applyGainLife(gameData, entry.getControllerId(), xValue);

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToTargetPlayerEffect} — deals a fixed amount of damage to the targeted player.
     */
    @HandlesEffect(DealDamageToTargetPlayerEffect.class)
    void resolveDealDamageToTargetPlayer(GameData gameData, StackEntry entry, DealDamageToTargetPlayerEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!isDamageSourcePreventedWithLog(gameData, entry)) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToTriggeringPermanentControllerEffect} — deals a fixed amount of damage to the
     * controller of an artifact that was put into a graveyard from the battlefield. The target player
     * UUID is pre-set on the stack entry at trigger-collection time.
     */
    @HandlesEffect(DealDamageToTriggeringPermanentControllerEffect.class)
    void resolveDealDamageToArtifactController(GameData gameData, StackEntry entry, DealDamageToTriggeringPermanentControllerEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!isDamageSourcePreventedWithLog(gameData, entry)) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToTargetPlayerByHandSizeEffect} — deals damage to the targeted player
     * equal to the number of cards in that player's hand.
     */
    @HandlesEffect(DealDamageToTargetPlayerByHandSizeEffect.class)
    void resolveDealDamageToTargetPlayerByHandSize(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!isDamageSourcePreventedWithLog(gameData, entry)) {
            List<Card> hand = gameData.playerHands.get(targetId);
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, hand != null ? hand.size() : 0);
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect} — deals damage to target player
     * equal to the number of cards of the specified type in the controller's graveyard.
     */
    @HandlesEffect(DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect.class)
    void resolveDealDamageToTargetPlayerEqualToCardTypeCountInGraveyard(
            GameData gameData, StackEntry entry, DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!isDamageSourcePreventedWithLog(gameData, entry)) {
            List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
            int count = 0;
            if (graveyard != null) {
                for (Card card : graveyard) {
                    if (card.getType() == effect.cardType() || card.getAdditionalTypes().contains(effect.cardType())) {
                        count++;
                    }
                }
            }

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, count);
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageIfFewCardsInHandEffect} — deals damage to the targeted player only if
     * they have at most the specified number of cards in hand (intervening-if condition rechecked at resolution).
     */
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

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToAnyTargetEffect} — deals a fixed amount of damage to any target
     * (creature or player), optionally preventing regeneration.
     */
    @HandlesEffect(DealDamageToAnyTargetEffect.class)
    void resolveDealDamageToAnyTarget(GameData gameData, StackEntry entry, DealDamageToAnyTargetEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, effect.cantRegenerate());
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToTargetAndTheirCreaturesEffect} — deals a fixed amount of damage
     * to target player or planeswalker AND each creature that player or that planeswalker's controller controls.
     */
    @HandlesEffect(DealDamageToTargetAndTheirCreaturesEffect.class)
    void resolveDealDamageToTargetAndTheirCreatures(GameData gameData, StackEntry entry, DealDamageToTargetAndTheirCreaturesEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        String cardName = entry.getCard().getName();

        if (isDamageSourcePreventedWithLog(gameData, entry)) return;

        // Determine the affected player: if target is a player, use directly;
        // if target is a planeswalker, use its controller
        UUID affectedPlayerId;
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        if (targetIsPlayer) {
            affectedPlayerId = targetId;
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        } else {
            Permanent targetPw = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPw == null) return;
            affectedPlayerId = gameQueryService.findPermanentController(gameData, targetId);
            if (affectedPlayerId == null) return;

            if (gameQueryService.hasProtectionFromSource(gameData, targetPw, entry.getCard())) {
                gameBroadcastService.logAndBroadcast(gameData,
                        cardName + "'s damage to " + targetPw.getCard().getName() + " is prevented.");
            } else {
                if (dealCreatureDamage(gameData, entry, targetPw, rawDamage)) {
                    destroyPermanent(gameData, targetPw);
                }
            }
        }

        // Deal damage to each creature the affected player controls
        List<Permanent> battlefield = gameData.playerBattlefields.get(affectedPlayerId);
        if (battlefield != null) {
            List<Permanent> destroyed = new ArrayList<>();
            for (Permanent creature : new ArrayList<>(battlefield)) {
                if (!gameQueryService.isCreature(gameData, creature)) continue;
                // Skip the planeswalker target (already dealt damage above)
                if (!targetIsPlayer && creature.getId().equals(targetId)) continue;
                if (gameQueryService.hasProtectionFromSource(gameData, creature, entry.getCard())) {
                    gameBroadcastService.logAndBroadcast(gameData,
                            cardName + "'s damage to " + creature.getCard().getName() + " is prevented.");
                    continue;
                }
                if (dealCreatureDamage(gameData, entry, creature, rawDamage)) {
                    destroyed.add(creature);
                }
            }
            destroyAllLethal(gameData, destroyed);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect} — deals damage equal to
     * the number of charge counters on the source to any target. The charge counter count is snapshotted
     * into xValue before sacrifice so it survives even if the source is no longer on the battlefield.
     */
    @HandlesEffect(DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect.class)
    void resolveDealDamageToAnyTargetEqualToChargeCounters(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int chargeCounters = entry.getXValue();
        if (chargeCounters <= 0) {
            String cardName = entry.getCard().getName();
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + " deals 0 damage (no charge counters).");
            return;
        }

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, chargeCounters);
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageEqualToSourcePowerToAnyTargetEffect} — deals damage equal to the source
     * permanent's power to any target. Does nothing if the source is no longer on the battlefield or has
     * zero or negative power.
     */
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
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealOrderedDamageToAnyTargetsEffect} — deals different damage amounts to an ordered
     * list of targets (each target may be a creature or player). For example, "deal 3 damage to target A
     * and 2 damage to target B".
     */
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
                if (!gameQueryService.hasProtectionFromSource(gameData, targetPermanent, entry.getCard())) {
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

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToAnyTargetAndGainLifeEffect} — deals a fixed amount of damage to any target
     * and the controller gains a fixed amount of life. The life gain occurs regardless of damage prevention.
     */
    @HandlesEffect(DealDamageToAnyTargetAndGainLifeEffect.class)
    void resolveDealDamageToAnyTargetAndGainLife(GameData gameData, StackEntry entry, DealDamageToAnyTargetAndGainLifeEffect effect) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);

        // Life gain is independent of damage prevention — always happens if the spell resolves
        lifeResolutionService.applyGainLife(gameData, entry.getControllerId(), effect.lifeGain());

        gameOutcomeService.checkWinCondition(gameData);
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
        int damage = damagePreventionService.applyCreaturePreventionShield(gameData, target, rawDamage);

        if (damageSource != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, damageSource.getId(), target, damage);
        } else if (entry.getSourcePermanentId() != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        }

        // Fire ON_DEALT_DAMAGE triggers (e.g. Nested Ghoul, Phyrexian Obliterator)
        if (damage > 0) {
            UUID sourceControllerId = damageSource != null
                    ? gameQueryService.findPermanentController(gameData, damageSource.getId())
                    : entry.getControllerId();
            triggerCollectionService.checkDealtDamageToCreatureTriggers(gameData, target, damage, sourceControllerId);
        }

        String sourceName = damageSource != null ? damageSource.getCard().getName() : entry.getCard().getName();

        boolean sourceHasInfect = gameQueryService.sourceHasKeyword(gameData, entry, damageSource, Keyword.INFECT);

        if (sourceHasInfect) {
            if (damage > 0 && !gameQueryService.cantHaveCounters(gameData, target)
                    && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
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

        boolean sourceHasDeathtouch = gameQueryService.sourceHasKeyword(gameData, entry, damageSource, Keyword.DEATHTOUCH);
        boolean isLethal = gameQueryService.isLethalDamage(damage, gameQueryService.getEffectiveToughness(gameData, target), sourceHasDeathtouch);
        if (isLethal) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        target.getCard().getName() + " is indestructible and survives.");
                return false;
            }
            return !graveyardService.tryRegenerate(gameData, target);
        }
        return false;
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
                || gameQueryService.hasProtectionFromSource(gameData, target, entry.getCard())) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + "'s damage is prevented.");
            return true;
        }
        return false;
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
                    || gameQueryService.hasProtectionFromSource(gameData, targetPermanent, entry.getCard())) {
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
            if (gameQueryService.hasProtectionFromSource(gameData, p, entry.getCard())) continue;
            if (dealCreatureDamage(gameData, entry, p, damage)) {
                destroyed.add(p);
            }
        }
        return destroyed;
    }

    private void dealDamageToPlayer(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        String cardName = entry.getCard().getName();
        if (damagePreventionService.isSourceDamagePreventedForPlayer(gameData, playerId, entry.getSourcePermanentId())
                || isSourcePermanentPreventedFromDealingDamage(gameData, entry)) {
            gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            return;
        }
        if (!damagePreventionService.applyColorDamagePreventionForPlayer(gameData, playerId, entry.getCard().getColor())) {
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, playerId, rawDamage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);

            boolean sourceHasInfect = gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.INFECT);
            boolean treatAsInfect = sourceHasInfect || gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId);

            if (treatAsInfect) {
                if (effectiveDamage > 0 && gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
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
                    triggerCollectionService.checkLifeLossTriggers(gameData, playerId, effectiveDamage);
                }
            }

            if (effectiveDamage > 0) {
                gameData.playersDealtDamageThisTurn.add(playerId);
                triggerCollectionService.checkDamageDealtToControllerTriggers(gameData, playerId, entry.getSourcePermanentId(), false);
            }
        }
    }

    /**
     * Resolves {@link FirstTargetDealsPowerDamageToSecondTargetEffect} — the first targeted creature deals
     * damage equal to its power to the second targeted creature ("bite" mechanic). Protection is checked
     * against the biting creature's color rather than the spell's color.
     */
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
        if (gameQueryService.hasProtectionFromSource(gameData, target, biter)) {
            CardColor biterColor = biter.getEffectiveColor();
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

    /**
     * Resolves {@link RevealTopCardDealManaValueDamageEffect} — reveals the top card of the target player's
     * library and deals damage equal to its mana value. Optionally damages the player, their creatures, or
     * returns the source to its owner's hand if the revealed card is a land.
     */
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

            gameOutcomeService.checkWinCondition(gameData);
        }

        if (effect.returnToHandIfLand() && topCard.getType() == CardType.LAND) {
            gameBroadcastService.logAndBroadcast(gameData,
                    "A land card was revealed — " + cardName + " is returned to its owner's hand.");
            entry.setReturnToHandAfterResolving(true);
        }
    }

    /**
     * Resolves {@link DealDamageToControllerEffect} — deals a fixed amount of damage to the spell or
     * ability's controller (self-damage, e.g. pain lands or drawback costs).
     */
    @HandlesEffect(DealDamageToControllerEffect.class)
    void resolveDealDamageToController(GameData gameData, StackEntry entry, DealDamageToControllerEffect effect) {
        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + "'s damage to controller is prevented.");
        } else {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
            dealDamageToPlayer(gameData, entry, entry.getControllerId(), rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link EnchantedCreatureDealsDamageToItsOwnerEffect} — the enchanted creature deals
     * damage to its owner (the player who originally owned it, not the current controller).
     * The damage source is the enchanted creature, not the aura.
     */
    @HandlesEffect(EnchantedCreatureDealsDamageToItsOwnerEffect.class)
    void resolveEnchantedCreatureDealsDamageToItsOwner(GameData gameData, StackEntry entry,
                                                        EnchantedCreatureDealsDamageToItsOwnerEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || aura.getAttachedTo() == null) return;

        Permanent creature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (creature == null) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return;

        UUID ownerId = gameData.stolenCreatures.getOrDefault(creature.getId(), controllerId);

        String creatureName = creature.getCard().getName();
        String ownerName = gameData.playerIdToName.get(ownerId);

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());

        // Create a temporary stack entry with the creature as source for correct damage attribution
        StackEntry creatureEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                creature.getCard(),
                controllerId,
                creatureName + " deals damage to its owner",
                List.of(),
                ownerId,
                creature.getId()
        );

        dealDamageToPlayer(gameData, creatureEntry, ownerId, rawDamage);
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToTargetControllerIfTargetHasKeywordEffect} — deals damage to the controller
     * of the targeted permanent, but only if that permanent has the specified keyword.
     */
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

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToTargetCreatureControllerEffect} — deals damage to the controller
     * of the targeted creature unconditionally.
     */
    @HandlesEffect(DealDamageToTargetCreatureControllerEffect.class)
    void resolveDealDamageToTargetCreatureController(
            GameData gameData,
            StackEntry entry,
            DealDamageToTargetCreatureControllerEffect effect
    ) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        String cardName = entry.getCard().getName();

        if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + "'s damage to " + gameData.playerIdToName.get(controllerId) + " is prevented.");
        } else {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
            dealDamageToPlayer(gameData, entry, controllerId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffect} — deals damage to each
     * opponent equal to the number of cards that opponent has drawn this turn. Opponents who drew no
     * cards take no damage.
     */
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

        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Deals divided damage to any number of targets (creatures and/or players) according
     * to the supplied assignments map. Called by {@code PermanentChoiceHandlerService}
     * after the player sacrifices an artifact for a divided-damage effect.
     */
    public void dealDividedDamageToAnyTargets(GameData gameData, Card sourceCard, UUID controllerId,
                                               Map<UUID, Integer> assignments) {
        if (assignments == null || assignments.isEmpty()) return;

        // Find source permanent on battlefield for damage tracking
        UUID sourcePermanentId = null;
        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf != null) {
            for (Permanent p : bf) {
                if (p.getCard() == sourceCard) {
                    sourcePermanentId = p.getId();
                    break;
                }
            }
        }

        // Create a temporary stack entry for the private damage helpers
        StackEntry tempEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                List.of(),
                null,
                sourcePermanentId
        );

        if (isDamageSourcePreventedWithLog(gameData, tempEntry)) return;

        List<Permanent> destroyed = new ArrayList<>();

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            UUID targetId = assignment.getKey();
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, assignment.getValue());

            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

            if (!targetIsPlayer && targetPermanent == null) continue;

            if (targetIsPlayer) {
                dealDamageToPlayer(gameData, tempEntry, targetId, rawDamage);
            } else {
                if (!gameQueryService.hasProtectionFromSource(gameData, targetPermanent, sourceCard)) {
                    if (dealCreatureDamage(gameData, tempEntry, targetPermanent, rawDamage)) {
                        destroyed.add(targetPermanent);
                    }
                } else {
                    gameBroadcastService.logAndBroadcast(gameData,
                            sourceCard.getName() + "'s damage to " + targetPermanent.getCard().getName() + " is prevented.");
                }
            }
        }

        destroyAllLethal(gameData, destroyed);
        gameOutcomeService.checkWinCondition(gameData);
    }

    /**
     * Resolves {@link SacrificeSelfAndDealDamageToDamagedPlayerEffect} — sacrifices the source creature
     * and deals fixed damage to the player that was dealt combat damage. Used by Furnace Scamp.
     */
    @HandlesEffect(SacrificeSelfAndDealDamageToDamagedPlayerEffect.class)
    void resolveSacrificeSelfAndDealDamageToDamagedPlayer(GameData gameData, StackEntry entry,
                                                          SacrificeSelfAndDealDamageToDamagedPlayerEffect effect) {
        UUID defenderId = entry.getTargetPermanentId();
        UUID sourcePermanentId = entry.getSourcePermanentId();

        if (defenderId == null || sourcePermanentId == null) {
            return;
        }

        // Check source creature is still on the battlefield
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getCard().getName() + "'s ability fizzles — source no longer on the battlefield.");
            return;
        }

        // Sacrifice the source creature
        permanentRemovalService.removePermanentToGraveyard(gameData, source);
        gameBroadcastService.logAndBroadcast(gameData,
                entry.getCard().getName() + " is sacrificed.");

        // Deal damage to the damaged player
        if (!gameData.playerIds.contains(defenderId)) {
            return;
        }
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());
        dealDamageToPlayer(gameData, entry, defenderId, rawDamage);
        gameOutcomeService.checkWinCondition(gameData);
    }
}

