package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BounceOwnCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControllerOnlyTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ExcludeSelfTargetFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class BounceResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(ReturnSelfToHandEffect.class,
                (gd, entry, effect) -> resolveReturnSelfToHand(gd, entry));
        registry.register(ReturnTargetPermanentToHandEffect.class,
                (gd, entry, effect) -> resolveReturnTargetPermanentToHand(gd, entry));
        registry.register(ReturnTargetCreatureToHandEffect.class,
                (gd, entry, effect) -> resolveReturnTargetPermanentToHand(gd, entry));
        registry.register(ReturnCreaturesToOwnersHandEffect.class,
                (gd, entry, effect) -> resolveReturnCreaturesToOwnersHand(gd, entry, (ReturnCreaturesToOwnersHandEffect) effect));
        registry.register(ReturnArtifactsTargetPlayerOwnsToHandEffect.class,
                (gd, entry, effect) -> resolveReturnArtifactsTargetPlayerOwnsToHand(gd, entry));
        registry.register(BounceOwnCreatureOnUpkeepEffect.class,
                (gd, entry, effect) -> resolveBounceOwnCreatureOnUpkeep(gd, entry));
        registry.register(ReturnSelfToHandOnCoinFlipLossEffect.class,
                (gd, entry, effect) -> resolveReturnSelfToHandOnCoinFlipLoss(gd, entry));
    }

    void resolveReturnSelfToHand(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<Card> hand = gameData.playerHands.get(controllerId);

        Permanent toReturn = null;
        for (Permanent p : battlefield) {
            if (p.getCard().getName().equals(entry.getCard().getName())) {
                toReturn = p;
                break;
            }
        }

        if (toReturn == null) {
            String logEntry = entry.getCard().getName() + " is no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        battlefield.remove(toReturn);
        gameHelper.removeOrphanedAuras(gameData);
        hand.add(toReturn.getOriginalCard());

        String logEntry = entry.getCard().getName() + " is returned to its owner's hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());
    }

    void resolveReturnTargetPermanentToHand(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.remove(target)) {
                gameHelper.removeOrphanedAuras(gameData);
                UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), playerId);
                gameData.stolenCreatures.remove(target.getId());
                List<Card> hand = gameData.playerHands.get(ownerId);
                hand.add(target.getOriginalCard());

                String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());

                break;
            }
        }
    }

    void resolveReturnCreaturesToOwnersHand(GameData gameData, StackEntry entry, ReturnCreaturesToOwnersHandEffect bounce) {
        UUID controllerId = entry.getControllerId();
        Set<UUID> affectedPlayers = new HashSet<>();

        boolean controllerOnly = bounce.filters().stream().anyMatch(f -> f instanceof ControllerOnlyTargetFilter);
        boolean excludeSelf = bounce.filters().stream().anyMatch(f -> f instanceof ExcludeSelfTargetFilter);

        List<UUID> playerIds = controllerOnly
                ? List.of(controllerId)
                : gameData.orderedPlayerIds;

        for (UUID playerId : playerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }

            List<Permanent> creaturesToReturn = battlefield.stream()
                    .filter(p -> gameQueryService.isCreature(gameData, p))
                    .filter(p -> !excludeSelf || !p.getOriginalCard().getId().equals(entry.getCard().getId()))
                    .toList();

            for (Permanent creature : creaturesToReturn) {
                battlefield.remove(creature);
                UUID ownerId = gameData.stolenCreatures.getOrDefault(creature.getId(), playerId);
                gameData.stolenCreatures.remove(creature.getId());
                List<Card> hand = gameData.playerHands.get(ownerId);
                hand.add(creature.getOriginalCard());
                affectedPlayers.add(ownerId);

                String logEntry = creature.getCard().getName() + " is returned to its owner's hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id, creature.getCard().getName(), entry.getCard().getName());
            }
        }

        if (!affectedPlayers.isEmpty()) {
            gameHelper.removeOrphanedAuras(gameData);
        }
    }

    void resolveReturnArtifactsTargetPlayerOwnsToHand(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> artifactsToReturn = battlefield.stream()
                .filter(p -> gameQueryService.isArtifact(p))
                .toList();

        if (artifactsToReturn.isEmpty()) {
            return;
        }

        for (Permanent artifact : artifactsToReturn) {
            battlefield.remove(artifact);
            List<Card> hand = gameData.playerHands.get(targetPlayerId);
            hand.add(artifact.getOriginalCard());

            String logEntry = artifact.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} returned to owner's hand by {}", gameData.id, artifact.getCard().getName(), entry.getCard().getName());
        }

        gameHelper.removeOrphanedAuras(gameData);
    }

    void resolveBounceOwnCreatureOnUpkeep(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

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
            String logEntry = playerName + " controls no creatures — nothing to return.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.BounceCreature(targetPlayerId));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                "Choose a creature you control to return to its owner's hand.");
    }

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


