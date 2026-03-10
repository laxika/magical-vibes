package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCreaturesCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CombatRestrictionResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @HandlesEffect(CantBlockSourceEffect.class)
    private void resolveCantBlockSource(GameData gameData, StackEntry entry, CantBlockSourceEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null || effect.sourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, effect.sourcePermanentId());
        String sourceName = source != null ? source.getCard().getName() : entry.getCard().getName();

        target.getCantBlockIds().add(effect.sourcePermanentId());

        String logEntry = target.getCard().getName() + " can't block " + sourceName + " this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} can't block {} this turn", gameData.id, target.getCard().getName(), sourceName);
    }

    @HandlesEffect(MustAttackThisTurnEffect.class)
    private void resolveMustAttackThisTurn(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setMustAttackThisTurn(true);
        // Force the creature to attack the ability's controller specifically, not their planeswalkers
        // (Scryfall ruling: "it must attack you, not the planeswalker")
        target.setMustAttackTargetId(entry.getControllerId());

        String controllerName = gameData.playerIdToName.get(entry.getControllerId());
        String logEntry = target.getCard().getName() + " must attack " + controllerName + " this turn if able.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} must attack {} this turn if able", gameData.id, target.getCard().getName(), controllerName);
    }

    @HandlesEffect(MustBlockSourceEffect.class)
    private void resolveMustBlockSource(GameData gameData, StackEntry entry, MustBlockSourceEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null || effect.sourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, effect.sourcePermanentId());
        String sourceName = source != null ? source.getCard().getName() : entry.getCard().getName();

        target.getMustBlockIds().add(effect.sourcePermanentId());

        String logEntry = target.getCard().getName() + " must block " + sourceName + " this turn if able.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} must block {} this turn if able", gameData.id, target.getCard().getName(), sourceName);
    }

    @HandlesEffect(TargetCreatureCantBlockThisTurnEffect.class)
    private void resolveCantBlockTargetCreature(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setCantBlockThisTurn(true);

        String logEntry = target.getCard().getName() + " can't block this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} can't block this turn", gameData.id, target.getCard().getName());
    }

    @HandlesEffect(TargetPlayerCreaturesCantBlockThisTurnEffect.class)
    private void resolveTargetPlayerCreaturesCantBlock(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetPermanentId();
        if (targetId == null) return;

        // Determine the affected player: if target is a player, use directly;
        // if target is a planeswalker, use its controller
        UUID affectedPlayerId;
        if (gameData.playerIds.contains(targetId)) {
            affectedPlayerId = targetId;
        } else {
            affectedPlayerId = gameQueryService.findPermanentController(gameData, targetId);
            if (affectedPlayerId == null) return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(affectedPlayerId);
        if (battlefield == null) return;

        String playerName = gameData.playerIdToName.get(affectedPlayerId);
        int count = 0;
        for (Permanent p : battlefield) {
            if (gameQueryService.isCreature(gameData, p)) {
                p.setCantBlockThisTurn(true);
                count++;
            }
        }

        if (count > 0) {
            String logEntry = "Creatures controlled by " + playerName + " can't block this turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} creatures controlled by {} can't block this turn", gameData.id, count, playerName);
        }
    }

    @HandlesEffect(CantBlockThisTurnEffect.class)
    private void resolveCantBlockThisTurn(GameData gameData, StackEntry entry,
                                          CantBlockThisTurnEffect effect) {
        int count = 0;
        for (UUID playerId : gameData.playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)
                        && (effect.filter() == null
                            || gameQueryService.matchesPermanentPredicate(gameData, p, effect.filter()))) {
                    p.setCantBlockThisTurn(true);
                    count++;
                }
            }
        }

        if (count > 0) {
            String logEntry = "Some creatures can't block this turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} creatures can't block this turn", gameData.id, count);
        }
    }

    @HandlesEffect(MakeCreatureUnblockableEffect.class)
    private void resolveMakeTargetUnblockable(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setCantBeBlocked(true);

        String logEntry = target.getCard().getName() + " can't be blocked this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} can't be blocked this turn", gameData.id, target.getCard().getName());
    }

    @HandlesEffect(MakeAllCreaturesUnblockableEffect.class)
    private void resolveMakeAllCreaturesUnblockable(GameData gameData, StackEntry entry) {
        gameData.forEachPermanent((playerId, perm) -> {
            if (gameQueryService.isCreature(gameData, perm)) {
                perm.setCantBeBlocked(true);
            }
        });

        String logEntry = "Creatures can't be blocked this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - All creatures can't be blocked this turn", gameData.id);
    }
}
