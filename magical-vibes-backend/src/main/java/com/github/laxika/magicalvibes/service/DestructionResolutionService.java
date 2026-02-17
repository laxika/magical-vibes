package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestructionResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(DestroyAllCreaturesEffect.class,
                (gd, entry, effect) -> resolveDestroyAllCreatures(gd, ((DestroyAllCreaturesEffect) effect).cannotBeRegenerated()));
        registry.register(DestroyAllEnchantmentsEffect.class,
                (gd, entry, effect) -> resolveDestroyAllEnchantments(gd));
        registry.register(DestroyTargetPermanentEffect.class,
                (gd, entry, effect) -> resolveDestroyTargetPermanent(gd, entry, (DestroyTargetPermanentEffect) effect));
        registry.register(DestroyTargetLandAndDamageControllerEffect.class,
                (gd, entry, effect) -> resolveDestroyTargetLandAndDamageController(gd, entry, (DestroyTargetLandAndDamageControllerEffect) effect));
        registry.register(DestroyCreatureBlockingThisEffect.class,
                (gd, entry, effect) -> resolveDestroyCreatureBlockingThis(gd, entry));
        registry.register(DestroyBlockedCreatureAndSelfEffect.class,
                (gd, entry, effect) -> resolveDestroyBlockedCreatureAndSelf(gd, entry));
        registry.register(SacrificeCreatureEffect.class,
                (gd, entry, effect) -> resolveSacrificeCreature(gd, entry));
        registry.register(EachOpponentSacrificesCreatureEffect.class,
                (gd, entry, effect) -> resolveEachOpponentSacrificesCreature(gd, entry));
        registry.register(SacrificeOtherCreatureOrDamageEffect.class,
                (gd, entry, effect) -> resolveSacrificeOtherCreatureOrDamage(gd, entry, (SacrificeOtherCreatureOrDamageEffect) effect));
    }

    void resolveDestroyAllCreatures(GameData gameData, boolean cannotBeRegenerated) {
        List<Permanent> toDestroy = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent perm : gameData.playerBattlefields.get(playerId)) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    toDestroy.add(perm);
                }
            }
        }

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
            if (!cannotBeRegenerated && gameHelper.tryRegenerate(gameData, perm)) {
                continue;
            }
            gameHelper.removePermanentToGraveyard(gameData, perm);
            String logEntry = perm.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed", gameData.id, perm.getCard().getName());
        }
    }

    void resolveDestroyAllEnchantments(GameData gameData) {
        List<Permanent> toDestroy = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent perm : gameData.playerBattlefields.get(playerId)) {
                if (perm.getCard().getType() == CardType.ENCHANTMENT) {
                    toDestroy.add(perm);
                }
            }
        }

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
            gameHelper.removePermanentToGraveyard(gameData, perm);
            String logEntry = perm.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed", gameData.id, perm.getCard().getName());
        }
    }

    void resolveDestroyTargetPermanent(GameData gameData, StackEntry entry, DestroyTargetPermanentEffect destroy) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (!destroy.targetTypes().contains(target.getCard().getType())) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (invalid target type).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            log.info("Game {} - {}'s ability fizzles, target type mismatch", gameData.id, entry.getCard().getName());
            return;
        }

        if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
            String logEntry = target.getCard().getName() + " is indestructible.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is indestructible, destroy prevented", gameData.id, target.getCard().getName());
            return;
        }

        if (gameQueryService.isCreature(gameData, target) && gameHelper.tryRegenerate(gameData, target)) {
            return;
        }

        gameHelper.removePermanentToGraveyard(gameData, target);
        String logEntry = target.getCard().getName() + " is destroyed.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is destroyed by {}'s ability",
                gameData.id, target.getCard().getName(), entry.getCard().getName());

        gameHelper.removeOrphanedAuras(gameData);
    }

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
        if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
            String logEntry = target.getCard().getName() + " is indestructible.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        } else {
            gameHelper.removePermanentToGraveyard(gameData, target);
            String logEntry = target.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
            gameHelper.removeOrphanedAuras(gameData);
        }

        // Deal damage to the land's controller regardless of whether destruction succeeded
        String cardName = entry.getCard().getName();
        int damage = effect.damage();

        if (!gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())
                && !gameHelper.applyColorDamagePreventionForPlayer(gameData, landControllerId, entry.getCard().getColor())) {
            int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, landControllerId, damage);
            effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, landControllerId, effectiveDamage, cardName);
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

    void resolveSacrificeCreature(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        performSacrificeCreatureForPlayer(gameData, targetPlayerId);
    }

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
                gameHelper.removePermanentToGraveyard(gameData, creature);
                String playerName = gameData.playerIdToName.get(targetPlayerId);
                String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrifices {}", gameData.id, playerName, creature.getCard().getName());
            }
            return;
        }

        // Multiple creatures — prompt player to choose
        gameData.permanentChoiceContext = new PermanentChoiceContext.SacrificeCreature(targetPlayerId);
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                "Choose a creature to sacrifice.");
    }

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
            int damage = effect.damage();

            if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
                String logEntry = cardName + "'s damage is prevented.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else if (!gameHelper.applyColorDamagePreventionForPlayer(gameData, controllerId, entry.getCard().getColor())) {
                int effectiveDamage = gameHelper.applyPlayerPreventionShield(gameData, controllerId, damage);
                effectiveDamage = gameHelper.redirectPlayerDamageToEnchantedCreature(gameData, controllerId, effectiveDamage, cardName);
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
                gameHelper.removePermanentToGraveyard(gameData, creature);
                String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName, creature.getCard().getName(), cardName);
            }
            return;
        }

        // Multiple other creatures — prompt player to choose
        gameData.permanentChoiceContext = new PermanentChoiceContext.SacrificeCreature(controllerId);
        playerInputService.beginPermanentChoice(gameData, controllerId, otherCreatureIds,
                "Choose a creature other than " + cardName + " to sacrifice.");
    }

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

        if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
            String logEntry = target.getCard().getName() + " is indestructible.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is indestructible, destroy prevented", gameData.id, target.getCard().getName());
            return;
        }

        if (gameHelper.tryRegenerate(gameData, target)) {
            return;
        }

        gameHelper.removePermanentToGraveyard(gameData, target);
        String logEntry = target.getCard().getName() + " is destroyed.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is destroyed by {}'s ability",
                gameData.id, target.getCard().getName(), entry.getCard().getName());

        gameHelper.removeOrphanedAuras(gameData);
    }

    void resolveDestroyBlockedCreatureAndSelf(GameData gameData, StackEntry entry) {
        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (attacker != null) {
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.INDESTRUCTIBLE)) {
                String logEntry = attacker.getCard().getName() + " is indestructible.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else if (!gameHelper.tryRegenerate(gameData, attacker)) {
                gameHelper.removePermanentToGraveyard(gameData, attacker);
                String logEntry = attacker.getCard().getName() + " is destroyed by " + entry.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} destroyed by {}'s block trigger", gameData.id, attacker.getCard().getName(), entry.getCard().getName());
            }
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self != null) {
            if (gameQueryService.hasKeyword(gameData, self, Keyword.INDESTRUCTIBLE)) {
                String logEntry = entry.getCard().getName() + " is indestructible.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            } else if (!gameHelper.tryRegenerate(gameData, self)) {
                gameHelper.removePermanentToGraveyard(gameData, self);
                String logEntry = entry.getCard().getName() + " is destroyed.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} destroyed (self-destruct from block trigger)", gameData.id, entry.getCard().getName());
            }
        }
    }
}
