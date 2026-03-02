package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndGainLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

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

        boolean skipRegeneration = effect.cannotBeRegenerated() || !canAttemptRegeneration(effect.targetTypes());
        destroyBatch(gameData, toDestroy, entry.getCard().getName(), skipRegeneration);
    }

    @HandlesEffect(DestroyNonlandPermanentsWithManaValueEqualToChargeCountersEffect.class)
    void resolveDestroyNonlandPermanentsByChargeCounterManaValue(GameData gameData, StackEntry entry) {
        destroyNonlandPermanentsByManaValue(gameData, entry.getXValue(), entry.getCard().getName(), null);
    }

    @HandlesEffect(DestroyNonlandPermanentsWithManaValueXDealtCombatDamageEffect.class)
    void resolveDestroyNonlandPermanentsByManaValueXDealtCombatDamage(GameData gameData, StackEntry entry) {
        String cardName = entry.getCard().getName();

        Set<UUID> damagedPlayerIds = gameData.combatDamageToPlayersThisTurn
                .getOrDefault(entry.getSourcePermanentId(), Set.of());
        if (damagedPlayerIds.isEmpty()) {
            String logEntry = cardName + " resolves but " + cardName + " dealt no combat damage to any player this turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} resolves but dealt no combat damage to any player this turn", gameData.id, cardName);
            return;
        }

        destroyNonlandPermanentsByManaValue(gameData, entry.getXValue(), cardName, damagedPlayerIds);
    }

    /**
     * Shared logic for destroying all nonland permanents with a given mana value.
     *
     * @param playerFilter if non-null, only permanents controlled by players in this set are considered
     */
    private void destroyNonlandPermanentsByManaValue(GameData gameData, int targetManaValue,
                                                      String cardName, Set<UUID> playerFilter) {
        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerFilter != null && !playerFilter.contains(playerId)) return;
            for (Permanent perm : battlefield) {
                if (perm.getCard().getType() == CardType.LAND
                        || perm.getCard().getAdditionalTypes().contains(CardType.LAND)) {
                    continue;
                }
                if (perm.getCard().getManaValue() == targetManaValue) {
                    toDestroy.add(perm);
                }
            }
        });

        if (toDestroy.isEmpty()) {
            String logEntry = cardName + " resolves but finds no nonland permanents with mana value " + targetManaValue + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} finds no nonland permanents with mana value {}", gameData.id, cardName, targetManaValue);
            return;
        }

        destroyBatch(gameData, toDestroy, cardName, false);
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

    /**
     * Simultaneously destroys a batch of permanents, respecting indestructible (snapshotted before
     * any removals) and optional regeneration.
     */
    private void destroyBatch(GameData gameData, List<Permanent> toDestroy, String sourceName,
                              boolean cannotBeRegenerated) {
        Set<Permanent> indestructible = new HashSet<>();
        for (Permanent perm : toDestroy) {
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.INDESTRUCTIBLE)) {
                indestructible.add(perm);
            }
        }

        for (Permanent perm : toDestroy) {
            if (indestructible.contains(perm)) {
                gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is indestructible.");
                continue;
            }
            if (!cannotBeRegenerated && gameHelper.tryRegenerate(gameData, perm)) {
                continue;
            }
            permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName() + " is destroyed.");
            log.info("Game {} - {} is destroyed by {}", gameData.id, perm.getCard().getName(), sourceName);
        }
    }

    private boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName) {
        return tryDestroyAndLog(gameData, target, sourceName, false);
    }

    private boolean tryDestroyAndLog(GameData gameData, Permanent target, String sourceName, boolean cannotBeRegenerated) {
        if (!permanentRemovalService.tryDestroyPermanent(gameData, target, cannotBeRegenerated)) {
            return false;
        }
        String logEntry = target.getCard().getName() + " is destroyed.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), sourceName);
        return true;
    }

    private void sacrificeAndLog(GameData gameData, Permanent creature, UUID playerId) {
        permanentRemovalService.removePermanentToGraveyard(gameData, creature);
        String playerName = gameData.playerIdToName.get(playerId);
        String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {}", gameData.id, playerName, creature.getCard().getName());
    }

    private void gainLifeForPlayer(GameData gameData, UUID playerId, int amount, String reason) {
        if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(playerId) + "'s life total can't change.");
            return;
        }
        int currentLife = gameData.playerLifeTotals.get(playerId);
        gameData.playerLifeTotals.put(playerId, currentLife + amount);
        String playerName = gameData.playerIdToName.get(playerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " gains " + amount + " life (" + reason + ").");
        log.info("Game {} - {} gains {} life ({})", gameData.id, playerName, amount, reason);
    }

    /**
     * Deals noncombat damage to a player with full prevention pipeline
     * (source prevention, color prevention, shields, redirection, life change check).
     */
    private void dealNoncombatDamageToPlayer(GameData gameData, UUID playerId, int baseDamage,
                                              String cardName, CardColor sourceColor) {
        int damage = gameQueryService.applyDamageMultiplier(gameData, baseDamage);

        if (gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)
                || gameHelper.applyColorDamagePreventionForPlayer(gameData, playerId, sourceColor)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            return;
        }

        int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, playerId, damage);
        effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);

        if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    gameData.playerIdToName.get(playerId) + "'s life total can't change.");
            return;
        }

        int currentLife = gameData.playerLifeTotals.getOrDefault(playerId, 20);
        gameData.playerLifeTotals.put(playerId, currentLife - effectiveDamage);

        if (effectiveDamage > 0) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + " deals " + effectiveDamage + " damage to " + playerName + ".");
            log.info("Game {} - {} deals {} damage to {}", gameData.id, cardName, effectiveDamage, playerName);
        }
    }

    private List<UUID> collectCreatureIds(GameData gameData, UUID playerId, Predicate<Permanent> additionalFilter) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<UUID> ids = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p) && additionalFilter.test(p)) {
                    ids.add(p.getId());
                }
            }
        }
        return ids;
    }

    @HandlesEffect(DestroyTargetPermanentEffect.class)
    void resolveDestroyTargetPermanent(GameData gameData, StackEntry entry, DestroyTargetPermanentEffect destroy) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        tryDestroyAndLog(gameData, target, entry.getCard().getName(), destroy.cannotBeRegenerated());
    }

    @HandlesEffect(DestroyEquipmentAttachedToTargetCreatureEffect.class)
    void resolveDestroyEquipmentAttachedToTargetCreature(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) {
            return;
        }

        List<Permanent> equipmentToDestroy = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (targetId.equals(p.getAttachedTo())
                    && p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                equipmentToDestroy.add(p);
            }
        });

        for (Permanent equipment : equipmentToDestroy) {
            tryDestroyAndLog(gameData, equipment, entry.getCard().getName());
        }
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
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Count ALL creatures that died this turn (across all players, including tokens)
        int totalDeaths = 0;
        for (int count : gameData.creatureDeathCountThisTurn.values()) {
            totalDeaths += count;
        }

        if (totalDeaths > 0) {
            if (!gameQueryService.canPlayerLifeChange(gameData, targetControllerId)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        gameData.playerIdToName.get(targetControllerId) + "'s life total can't change.");
            } else {
                int currentLife = gameData.playerLifeTotals.getOrDefault(targetControllerId, 20);
                gameData.playerLifeTotals.put(targetControllerId, currentLife - totalDeaths);

                String playerName = gameData.playerIdToName.get(targetControllerId);
                String lifeLog = playerName + " loses " + totalDeaths + " life (" + entry.getCard().getName() + ").";
                gameBroadcastService.logAndBroadcast(gameData, lifeLog);
                log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, totalDeaths, entry.getCard().getName());
            }
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
        UUID landControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (landControllerId == null) {
            return;
        }

        // Attempt to destroy the land
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Deal damage to the land's controller regardless of whether destruction succeeded
        dealNoncombatDamageToPlayer(gameData, landControllerId, effect.damage(),
                entry.getCard().getName(), entry.getCard().getColor());

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
        List<UUID> attackingCreatureIds = collectCreatureIds(gameData, targetPlayerId, Permanent::isAttacking);

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
                    sacrificeAndLog(gameData, creature, targetPlayerId);
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
        List<UUID> creatureIds = collectCreatureIds(gameData, targetPlayerId, p -> true);

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
                sacrificeAndLog(gameData, creature, targetPlayerId);
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
        String cardName = entry.getCard().getName();
        UUID sourceCardId = entry.getCard().getId();

        List<UUID> otherCreatureIds = collectCreatureIds(gameData, controllerId,
                p -> !p.getCard().getId().equals(sourceCardId));

        if (otherCreatureIds.isEmpty()) {
            // Can't sacrifice — deal damage to controller
            dealNoncombatDamageToPlayer(gameData, controllerId, effect.damage(), cardName, entry.getCard().getColor());
            gameHelper.checkWinCondition(gameData);
            return;
        }

        if (otherCreatureIds.size() == 1) {
            // Only one other creature — sacrifice it automatically
            Permanent creature = gameQueryService.findPermanentById(gameData, otherCreatureIds.getFirst());
            if (creature != null) {
                sacrificeAndLog(gameData, creature, controllerId);
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

        tryDestroyAndLog(gameData, target, entry.getCard().getName());
    }

    @HandlesEffect(DestroyBlockedCreatureAndSelfEffect.class)
    void resolveDestroyBlockedCreatureAndSelf(GameData gameData, StackEntry entry) {
        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (attacker != null) {
            tryDestroyAndLog(gameData, attacker, entry.getCard().getName());
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null) {
            tryDestroyAndLog(gameData, self, entry.getCard().getName());
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
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Boost self by mana value regardless of destruction result
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null && manaValue > 0) {
            self.setPowerModifier(self.getPowerModifier() + manaValue);

            String boostLog = entry.getCard().getName() + " gets +" + manaValue + "/+0 until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, boostLog);
            log.info("Game {} - {} gets +{}/+0 from {}'s mana value", gameData.id, entry.getCard().getName(), manaValue, target.getCard().getName());
        }
    }

    @HandlesEffect(DestroyTargetPermanentAndGainLifeEqualToManaValueEffect.class)
    void resolveDestroyTargetPermanentAndGainLifeEqualToManaValue(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        int manaValue = target.getCard().getManaValue();

        // Attempt to destroy the target
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Gain life equal to mana value regardless of destruction result
        if (manaValue > 0) {
            gainLifeForPlayer(gameData, entry.getControllerId(), manaValue,
                    "equal to " + target.getCard().getName() + "'s mana value");
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
        tryDestroyAndLog(gameData, target, entry.getCard().getName());

        // Gain life equal to toughness regardless of destruction result
        gainLifeForPlayer(gameData, entry.getControllerId(), toughness,
                "equal to " + target.getCard().getName() + "'s toughness");
    }
}
