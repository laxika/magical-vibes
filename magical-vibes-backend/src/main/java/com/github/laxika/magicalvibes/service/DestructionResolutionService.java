package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
class DestructionResolutionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

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
