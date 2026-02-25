package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestructionResolutionService {

    private final GameHelper gameHelper;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @HandlesEffect(DestroyAllPermanentsEffect.class)
    void resolveDestroyAllPermanents(GameData gameData, StackEntry entry, DestroyAllPermanentsEffect effect) {
        List<Permanent> toDestroy = new ArrayList<>();
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (effect.onlyOpponents() && playerId.equals(controllerId)) {
                return;
            }
            for (Permanent perm : battlefield) {
                if (matchesDestroyAllTargetType(gameData, perm, effect.targetTypes())
                        && (effect.filter() == null
                            || gameQueryService.matchesPermanentPredicate(perm, effect.filter(), filterContext))) {
                    toDestroy.add(perm);
                }
            }
        });

        // Snapshot indestructible status before any removals (MTG rules: "destroy all" is simultaneous)
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                String logEntry = perm.getCard().getName() + " is indestructible.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                continue;
            }
            if (canAttemptRegeneration(effect.targetTypes())
                    && !effect.cannotBeRegenerated()
                    && gameHelper.tryRegenerate(gameData, perm)) {
                continue;
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            String logEntry = perm.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed", gameData.id, perm.getCard().getName());
        }
    }

    private boolean matchesDestroyAllTargetType(GameData gameData, Permanent permanent, Set<CardType> targetTypes) {
        for (CardType targetType : targetTypes) {
            if (targetType == CardType.CREATURE && gameQueryService.isCreature(gameData, permanent)) {
                return true;
            }
            if (targetType == CardType.ARTIFACT && gameQueryService.isArtifact(permanent)) {
                return true;
            }
            if (permanent.getCard().getType() == targetType) {
                return true;
            }
        }
        return false;
    }

    private boolean canAttemptRegeneration(Set<CardType> targetTypes) {
        return targetTypes.contains(CardType.CREATURE) || targetTypes.contains(CardType.ARTIFACT);
    }

    @HandlesEffect(DestroyTargetPermanentEffect.class)
    void resolveDestroyTargetPermanent(GameData gameData, StackEntry entry, DestroyTargetPermanentEffect destroy) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (!permanentRemovalService.tryDestroyPermanent(gameData, target, destroy.cannotBeRegenerated())) {
            return;
        }
        String logEntry = target.getCard().getName() + " is destroyed.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is destroyed by {}'s ability",
                gameData.id, target.getCard().getName(), entry.getCard().getName());
    }

    @HandlesEffect(DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect.class)
    void resolveDestroyTargetAndControllerLosesLifePerCreatureDeaths(GameData gameData, StackEntry entry,
                                                                     DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        // Find the controller before destruction
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, entry.getTargetPermanentId());
        if (targetControllerId == null) {
            return;
        }

        // Destroy the target creature
        if (permanentRemovalService.tryDestroyPermanent(gameData, target)) {
            String logEntry = target.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        // Count ALL creatures that died this turn (across all players, including tokens)
        int totalDeaths = 0;
        for (int count : gameData.creatureDeathCountThisTurn.values()) {
            totalDeaths += count;
        }

        if (totalDeaths > 0) {
            int currentLife = gameData.playerLifeTotals.getOrDefault(targetControllerId, 20);
            gameData.playerLifeTotals.put(targetControllerId, currentLife - totalDeaths);

            String playerName = gameData.playerIdToName.get(targetControllerId);
            String lifeLog = playerName + " loses " + totalDeaths + " life (" + entry.getCard().getName() + ").";
            gameBroadcastService.logAndBroadcast(gameData, lifeLog);
            log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, totalDeaths, entry.getCard().getName());
        }

        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(DestroyTargetLandAndDamageControllerEffect.class)
    void resolveDestroyTargetLandAndDamageController(GameData gameData, StackEntry entry,
                                                      DestroyTargetLandAndDamageControllerEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (target.getCard().getType() != CardType.LAND) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (invalid target type).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        // Find the controller of the targeted land before destruction
        UUID landControllerId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(target)) {
                landControllerId = playerId;
                break;
            }
        }

        if (landControllerId == null) {
            return;
        }

        // Attempt to destroy the land
        if (permanentRemovalService.tryDestroyPermanent(gameData, target)) {
            String logEntry = target.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
        }

        // Deal damage to the land's controller regardless of whether destruction succeeded
        String cardName = entry.getCard().getName();
        int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());

        if (!gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())
                && !gameHelper.applyColorDamagePreventionForPlayer(gameData, landControllerId, entry.getCard().getColor())) {
            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, landControllerId, damage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, landControllerId, effectiveDamage, cardName);
            int currentLife = gameData.playerLifeTotals.getOrDefault(landControllerId, 20);
            gameData.playerLifeTotals.put(landControllerId, currentLife - effectiveDamage);

            if (effectiveDamage > 0) {
                String playerName = gameData.playerIdToName.get(landControllerId);
                String damageLog = playerName + " takes " + effectiveDamage + " damage from " + cardName + ".";
                gameBroadcastService.logAndBroadcast(gameData, damageLog);
                log.info("Game {} - {} takes {} damage from {}", gameData.id, playerName, effectiveDamage, cardName);
            }
        } else {
            String preventLog = cardName + "'s damage to " + gameData.playerIdToName.get(landControllerId) + " is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, preventLog);
        }

        gameHelper.checkWinCondition(gameData);
    }

    @HandlesEffect(SacrificeCreatureEffect.class)
    void resolveSacrificeCreature(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        performSacrificeCreatureForPlayer(gameData, targetPlayerId);
    }

    @HandlesEffect(SacrificeAttackingCreaturesEffect.class)
    void resolveSacrificeAttackingCreatures(GameData gameData, StackEntry entry, SacrificeAttackingCreaturesEffect effect) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        // Check metalcraft at resolution time (intervening-if)
        UUID controllerId = entry.getControllerId();
        List<Permanent> controllerBattlefield = gameData.playerBattlefields.get(controllerId);
        long artifactCount = 0;
        if (controllerBattlefield != null) {
            artifactCount = controllerBattlefield.stream()
                    .filter(gameQueryService::isArtifact)
                    .count();
        }
        int count = artifactCount >= 3 ? effect.metalcraftCount() : effect.baseCount();

        // Collect attacking creatures on target player's battlefield
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        List<UUID> attackingCreatureIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.isAttacking() && gameQueryService.isCreature(gameData, p)) {
                    attackingCreatureIds.add(p.getId());
                }
            }
        }

        if (attackingCreatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no attacking creatures to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no attacking creatures to sacrifice", gameData.id, playerName);
            return;
        }

        if (attackingCreatureIds.size() <= count) {
            // Auto-sacrifice all attacking creatures
            for (UUID creatureId : attackingCreatureIds) {
                Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
                if (creature != null) {
                    permanentRemovalService.removePermanentToGraveyard(gameData, creature);
                    String playerName = gameData.playerIdToName.get(targetPlayerId);
                    String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} sacrifices {}", gameData.id, playerName, creature.getCard().getName());
                }
            }
            return;
        }

        // More attacking creatures than required — prompt player to choose
        gameData.pendingSacrificeAttackingCreature = true;
        playerInputService.beginMultiPermanentChoice(gameData, targetPlayerId, attackingCreatureIds,
                count, "Choose " + count + " attacking creature" + (count > 1 ? "s" : "") + " to sacrifice.");
    }

    @HandlesEffect(EachOpponentSacrificesCreatureEffect.class)
    void resolveEachOpponentSacrificesCreature(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            performSacrificeCreatureForPlayer(gameData, playerId);
        }
    }

    void performSacrificeCreatureForPlayer(GameData gameData, UUID targetPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        List<UUID> creatureIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            String logEntry = playerName + " has no creatures to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures to sacrifice", gameData.id, playerName);
            return;
        }

        if (creatureIds.size() == 1) {
            // Only one creature — sacrifice it automatically
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                permanentRemovalService.removePermanentToGraveyard(gameData, creature);
                String playerName = gameData.playerIdToName.get(targetPlayerId);
                String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrifices {}", gameData.id, playerName, creature.getCard().getName());
            }
            return;
        }

        // Multiple creatures — prompt player to choose
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreature(targetPlayerId));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                "Choose a creature to sacrifice.");
    }

    @HandlesEffect(SacrificeOtherCreatureOrDamageEffect.class)
    void resolveSacrificeOtherCreatureOrDamage(GameData gameData, StackEntry entry, SacrificeOtherCreatureOrDamageEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        UUID sourceCardId = entry.getCard().getId();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> otherCreatureIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p) && !p.getCard().getId().equals(sourceCardId)) {
                    otherCreatureIds.add(p.getId());
                }
            }
        }

        if (otherCreatureIds.isEmpty()) {
            // Can't sacrifice — deal damage to controller
            int damage = gameQueryService.applyDamageMultiplier(gameData, effect.damage());

            if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
                String logEntry = cardName + "'s damage is prevented.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, controllerId, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, controllerId, damage);
                effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, controllerId, effectiveDamage, cardName);
                int currentLife = gameData.playerLifeTotals.getOrDefault(controllerId, 20);
                gameData.playerLifeTotals.put(controllerId, currentLife - effectiveDamage);

                if (effectiveDamage > 0) {
                    String logEntry = cardName + " deals " + effectiveDamage + " damage to " + playerName + ".";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} deals {} damage to {} (no creatures to sacrifice)", gameData.id, cardName, effectiveDamage, playerName);
                }
            }

            gameHelper.checkWinCondition(gameData);
            return;
        }

        if (otherCreatureIds.size() == 1) {
            // Only one other creature — sacrifice it automatically
            Permanent creature = gameQueryService.findPermanentById(gameData, otherCreatureIds.getFirst());
            if (creature != null) {
                permanentRemovalService.removePermanentToGraveyard(gameData, creature);
                String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName, creature.getCard().getName(), cardName);
            }
            return;
        }

        // Multiple other creatures — prompt player to choose
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreature(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, otherCreatureIds,
                "Choose a creature other than " + cardName + " to sacrifice.");
    }

    @HandlesEffect(DestroyCreatureBlockingThisEffect.class)
    void resolveDestroyCreatureBlockingThis(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (!gameQueryService.isCreature(gameData, target)) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (invalid target).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            return;
        }

        if (!permanentRemovalService.tryDestroyPermanent(gameData, target)) {
            return;
        }
        String logEntry = target.getCard().getName() + " is destroyed.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is destroyed by {}'s ability",
                gameData.id, target.getCard().getName(), entry.getCard().getName());
    }

    @HandlesEffect(DestroyBlockedCreatureAndSelfEffect.class)
    void resolveDestroyBlockedCreatureAndSelf(GameData gameData, StackEntry entry) {
        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (attacker != null && permanentRemovalService.tryDestroyPermanent(gameData, attacker)) {
            String logEntry = attacker.getCard().getName() + " is destroyed by " + entry.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} destroyed by {}'s block trigger", gameData.id, attacker.getCard().getName(), entry.getCard().getName());
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null && permanentRemovalService.tryDestroyPermanent(gameData, self)) {
            String logEntry = entry.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} destroyed (self-destruct from block trigger)", gameData.id, entry.getCard().getName());
        }
    }

    @HandlesEffect(DestroyTargetPermanentAndBoostSelfByManaValueEffect.class)
    void resolveDestroyTargetArtifactAndBoostSelfByManaValue(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        int manaValue = target.getCard().getManaValue();

        // Attempt to destroy the artifact
        if (permanentRemovalService.tryDestroyPermanent(gameData, target)) {
            String logEntry = target.getCard().getName() + " is destroyed by " + entry.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} destroyed by {}'s ability", gameData.id, target.getCard().getName(), entry.getCard().getName());
        }

        // Boost self by mana value regardless of destruction result
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null && manaValue > 0) {
            self.setPowerModifier(self.getPowerModifier() + manaValue);

            String boostLog = entry.getCard().getName() + " gets +" + manaValue + "/+0 until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, boostLog);
            log.info("Game {} - {} gets +{}/+0 from {}'s mana value", gameData.id, entry.getCard().getName(), manaValue, target.getCard().getName());
        }
    }

    @HandlesEffect(DestroyTargetCreatureAndGainLifeEqualToToughnessEffect.class)
    void resolveDestroyTargetCreatureAndGainLifeEqualToToughness(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        // Attempt to destroy (life gain happens regardless)
        if (permanentRemovalService.tryDestroyPermanent(gameData, target)) {
            String logEntry = target.getCard().getName() + " is destroyed by " + entry.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} destroyed by {}'s ability", gameData.id, target.getCard().getName(), entry.getCard().getName());
        }

        // Gain life equal to toughness regardless of destruction result
        UUID controllerId = entry.getControllerId();
        int currentLife = gameData.playerLifeTotals.get(controllerId);
        gameData.playerLifeTotals.put(controllerId, currentLife + toughness);

        String playerName = gameData.playerIdToName.get(controllerId);
        String lifeLog = playerName + " gains " + toughness + " life (equal to " + target.getCard().getName() + "'s toughness).";
        gameBroadcastService.logAndBroadcast(gameData, lifeLog);
        log.info("Game {} - {} gains {} life from {}'s toughness", gameData.id, playerName, toughness, target.getCard().getName());
    }
}


