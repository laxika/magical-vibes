package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BounceCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class BounceResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;

    @HandlesEffect(ReturnSelfToHandEffect.class)
    void resolveReturnSelfToHand(GameData gameData, StackEntry entry) {
        Permanent toReturn = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());

        if (toReturn == null) {
            String logEntry = entry.getCard().getName() + " is no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        permanentRemovalService.removePermanentToHand(gameData, toReturn);
        permanentRemovalService.removeOrphanedAuras(gameData);

        String logEntry = entry.getCard().getName() + " is returned to its owner's hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());
    }

    @HandlesEffect(ReturnTargetPermanentToHandEffect.class)
    void resolveReturnTargetPermanentToHand(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            permanentRemovalService.removeOrphanedAuras(gameData);

            String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
        }
    }

    @HandlesEffect(ReturnCreaturesToOwnersHandEffect.class)
    void resolveReturnCreaturesToOwnersHand(GameData gameData, StackEntry entry, ReturnCreaturesToOwnersHandEffect bounce) {
        List<Permanent> creaturesToReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                creaturesToReturn.addAll(battlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .filter(p -> gameQueryService.matchesFilters(
                                p,
                                bounce.filters(),
                                FilterContext.of(gameData)
                                        .withSourceCardId(entry.getCard().getId())
                                        .withSourceControllerId(entry.getControllerId())))
                        .toList()));

        for (Permanent creature : creaturesToReturn) {
            permanentRemovalService.removePermanentToHand(gameData, creature);

            String logEntry = creature.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, creature.getCard().getName(), entry.getCard().getName());
        }

        if (!creaturesToReturn.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    @HandlesEffect(ReturnArtifactsTargetPlayerOwnsToHandEffect.class)
    void resolveReturnArtifactsTargetPlayerOwnsToHand(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> artifactsToReturn = new ArrayList<>();
        gameData.forEachBattlefield((controllingPlayerId, battlefield) ->
                artifactsToReturn.addAll(battlefield.stream()
                        .filter(gameQueryService::isArtifact)
                        .filter(p -> {
                            UUID ownerId = gameData.stolenCreatures.getOrDefault(p.getId(), controllingPlayerId);
                            return ownerId.equals(targetPlayerId);
                        })
                        .toList()));

        for (Permanent artifact : artifactsToReturn) {
            permanentRemovalService.removePermanentToHand(gameData, artifact);

            String logEntry = artifact.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, artifact.getCard().getName(), entry.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    @HandlesEffect(BounceCreatureOnUpkeepEffect.class)
    void resolveBounceCreatureOnUpkeep(GameData gameData, StackEntry entry, BounceCreatureOnUpkeepEffect bounceEffect) {
        UUID choosingPlayerId = switch (bounceEffect.scope()) {
            case SOURCE_CONTROLLER -> entry.getControllerId();
            case TRIGGER_TARGET_PLAYER -> entry.getTargetPermanentId() != null
                    ? entry.getTargetPermanentId()
                    : entry.getControllerId();
        };
        String playerName = gameData.playerIdToName.get(choosingPlayerId);

        List<Permanent> battlefield = gameData.playerBattlefields.get(choosingPlayerId);
        List<UUID> creatureIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)
                        && gameQueryService.matchesFilters(
                        p,
                        bounceEffect.filters(),
                        FilterContext.of(gameData)
                                .withSourceCardId(entry.getCard().getId())
                                .withSourceControllerId(entry.getControllerId()))) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String logEntry = playerName + " controls no valid creatures — nothing to return.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BounceCreature(choosingPlayerId));
        playerInputService.beginPermanentChoice(gameData, choosingPlayerId, creatureIds, bounceEffect.prompt());
    }

    @HandlesEffect(ReturnSelfToHandOnCoinFlipLossEffect.class)
    void resolveReturnSelfToHandOnCoinFlipLoss(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        boolean wonFlip = ThreadLocalRandom.current().nextBoolean();

        String flipLog = wonFlip
                ? gameData.playerIdToName.get(controllerId) + " wins the coin flip for " + sourceName + "."
                : gameData.playerIdToName.get(controllerId) + " loses the coin flip for " + sourceName + ".";
        gameBroadcastService.logAndBroadcast(gameData, flipLog);

        if (wonFlip) {
            return;
        }

        resolveReturnSelfToHand(gameData, entry);
    }
}
