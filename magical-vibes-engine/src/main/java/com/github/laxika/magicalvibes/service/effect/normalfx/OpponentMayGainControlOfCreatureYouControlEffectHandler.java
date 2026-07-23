package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMayGainControlOfCreatureYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link OpponentMayGainControlOfCreatureYouControlEffect}: offers an opponent a may-prompt
 * to take one creature the source's controller controls for the effect's control duration.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpponentMayGainControlOfCreatureYouControlEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentMayGainControlOfCreatureYouControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        offer(gameData, entry, (OpponentMayGainControlOfCreatureYouControlEffect) effect);
    }

    /**
     * Starts the opponent may-steal interaction (also used as a {@code ForcedCostOrElse} fallback).
     */
    public void offer(GameData gameData, StackEntry entry, OpponentMayGainControlOfCreatureYouControlEffect effect) {
        UUID victimControllerId = entry.getControllerId();
        UUID opponentId = firstOpponent(gameData, victimControllerId);
        if (opponentId == null) {
            return;
        }

        List<UUID> creatureIds = creatureIdsControlledBy(gameData, victimControllerId);
        if (creatureIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(entry.getCard().getName() + " — no creatures for the opponent to take."));
            log.info("Game {} - {} opponent may-steal skipped (no creatures)", gameData.id, entry.getCard().getName());
            return;
        }

        String prompt = entry.getCard().getName()
                + " - Gain control of a creature "
                + gameData.playerIdToName.get(victimControllerId)
                + " controls?";
        // PendingMayAbility.controllerId = decision maker (opponent).
        // targetCardId field reused to carry the victim controller id (see Tempest Efreet pattern).
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), opponentId, List.of(effect), prompt,
                victimControllerId, null, entry.getSourcePermanentId()));
    }

    /**
     * Applies control after the opponent accepted (and chose a creature when needed).
     * Wraps a {@link GainControlOfTargetEffect} so the floating effect is recognized as L2 control.
     */
    public void applyControl(GameData gameData, UUID opponentId, UUID sourcePermanentId,
            String sourceCardName, OpponentMayGainControlOfCreatureYouControlEffect effect, Permanent creature) {
        if (creature == null || sourcePermanentId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(sourceCardName + "'s ability has no effect (source left the battlefield)."));
            return;
        }
        var controlEffect = new GainControlOfTargetEffect(effect.duration());
        creatureControlService.applyControlEffect(gameData, opponentId, creature,
                controlEffect, effect.duration().toEffectDuration(), sourcePermanentId, sourceCardName);
        log.info("Game {} - {} gains control of {} via {}", gameData.id,
                gameData.playerIdToName.get(opponentId), creature.getCard().getName(), sourceCardName);
    }

    public void beginCreatureChoice(GameData gameData, UUID opponentId, UUID victimControllerId,
            UUID sourcePermanentId, String sourceCardName,
            OpponentMayGainControlOfCreatureYouControlEffect effect) {
        List<UUID> creatureIds = creatureIdsControlledBy(gameData, victimControllerId);
        if (creatureIds.isEmpty()) {
            return;
        }
        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            applyControl(gameData, opponentId, sourcePermanentId, sourceCardName, effect, creature);
            return;
        }
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.OpponentMayGainControlOfCreatureYouControl(
                        opponentId, victimControllerId, sourcePermanentId, sourceCardName, effect.duration()));
        playerInputService.beginPermanentChoice(gameData, opponentId, creatureIds,
                "Choose a creature to gain control of.");
    }

    public List<UUID> creatureIdsControlledBy(GameData gameData, UUID controllerId) {
        List<UUID> ids = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return ids;
        }
        for (Permanent perm : battlefield) {
            if (gameQueryService.isCreature(gameData, perm)) {
                ids.add(perm.getId());
            }
        }
        return ids;
    }

    private static UUID firstOpponent(GameData gameData, UUID controllerId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                return playerId;
            }
        }
        return null;
    }
}
