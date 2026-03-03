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

/**
 * Resolves bounce effects that return permanents from the battlefield to their owner's hand.
 * Handles single-target bounce (Boomerang), self-bounce (Viashino Sandscout), mass creature
 * bounce (Evacuation), artifact bounce by owner (Hurkyl's Recall), upkeep-triggered creature
 * bounce with player choice (Sunken Hope), and coin-flip-based self-bounce (Scoria Wurm).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BounceResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;

    /**
     * Returns the source permanent to its owner's hand. Uses {@code sourcePermanentId} to
     * identify the exact permanent. If the permanent has left the battlefield before resolution,
     * logs a message and does nothing.
     *
     * @param gameData the current game state
     * @param entry    the stack entry containing the source permanent ID
     */
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

    /**
     * Returns the targeted permanent to its owner's hand. Uses {@code targetPermanentId} from
     * the stack entry to locate the target. If the target is no longer on the battlefield
     * (e.g. destroyed or already bounced), the effect fizzles silently.
     *
     * @param gameData the current game state
     * @param entry    the stack entry containing the target permanent ID
     */
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

    /**
     * Returns all creatures matching the effect's filters to their owners' hands. Iterates
     * every battlefield, collects matching creatures, then bounces them all. Orphaned auras
     * are cleaned up once after all creatures have been returned.
     *
     * @param gameData the current game state
     * @param entry    the stack entry providing source card and controller context for filters
     * @param bounce   the effect record containing the creature filters
     */
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

    /**
     * Returns all artifacts owned by the targeted player to that player's hand, regardless
     * of who controls them. Ownership is resolved via {@code stolenCreatures} to correctly
     * handle stolen artifacts (e.g. via Control Magic). The target player ID is stored in
     * {@code targetPermanentId} on the stack entry.
     *
     * @param gameData the current game state
     * @param entry    the stack entry containing the target player ID and source card info
     */
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

    /**
     * Handles upkeep-triggered bounce effects that require a player to choose a creature to
     * return. The choosing player is determined by the effect's scope: either the source's
     * controller or the triggered ability's target player. Collects eligible creature IDs
     * based on the effect's filters, then prompts the player to choose one. If no valid
     * creatures exist, logs a message and does nothing.
     *
     * @param gameData     the current game state
     * @param entry        the stack entry providing controller and target player context
     * @param bounceEffect the effect record containing scope, filters, and prompt text
     */
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

    /**
     * Flips a coin for the source permanent's controller. On a win, the permanent stays on
     * the battlefield. On a loss, delegates to {@link #resolveReturnSelfToHand} to return
     * the source permanent to its owner's hand.
     *
     * @param gameData the current game state
     * @param entry    the stack entry containing the source permanent and controller info
     */
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
