package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingRagingRiver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RagingRiverEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RagingRiverEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RagingRiverEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> attackerIds = new ArrayList<>();
        Map<UUID, UUID> defendingPlayerByAttackerId = new LinkedHashMap<>();
        Set<UUID> defendingPlayerSet = new LinkedHashSet<>();

        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent attacker : battlefield) {
                if (!attacker.isAttacking()
                        || !controllerId.equals(gameQueryService.findPermanentController(gameData, attacker.getId()))) {
                    continue;
                }
                UUID defendingPlayerId = findDefendingPlayer(gameData, attacker.getAttackTarget());
                if (defendingPlayerId == null) {
                    continue;
                }
                attackerIds.add(attacker.getId());
                defendingPlayerByAttackerId.put(attacker.getId(), defendingPlayerId);
                defendingPlayerSet.add(defendingPlayerId);
            }
        }

        if (attackerIds.isEmpty()) {
            return;
        }

        List<UUID> defendingPlayerIds = new ArrayList<>(defendingPlayerSet);
        Map<UUID, List<UUID>> groundCreatureIdsByDefendingPlayer = new LinkedHashMap<>();
        for (UUID defendingPlayerId : defendingPlayerIds) {
            List<UUID> groundCreatureIds = gameData.playerBattlefields
                    .getOrDefault(defendingPlayerId, List.of()).stream()
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent)
                            && !gameQueryService.hasKeyword(gameData, permanent, Keyword.FLYING))
                    .map(Permanent::getId)
                    .toList();
            groundCreatureIdsByDefendingPlayer.put(defendingPlayerId, groundCreatureIds);
        }

        PendingRagingRiver state = new PendingRagingRiver(controllerId, attackerIds,
                defendingPlayerByAttackerId, defendingPlayerIds, groundCreatureIdsByDefendingPlayer,
                Map.of(), Map.of(), 0, 0);
        advanceDefendingPlayerChoice(gameData, state);
    }

    /** Completes one defending player's division of their nonflying creatures. */
    public void completeDefendingPlayerChoice(GameData gameData, List<UUID> pile1Ids) {
        PendingRagingRiver state = gameData.pollPendingInteraction(PendingRagingRiver.class);
        if (state == null || state.nextDefendingPlayerIndex() >= state.defendingPlayerIds().size()) {
            throw new IllegalStateException("No pending Raging River pile choice");
        }

        UUID defendingPlayerId = state.defendingPlayerIds().get(state.nextDefendingPlayerIndex());
        List<UUID> groundCreatureIds = state.groundCreatureIdsByDefendingPlayer()
                .getOrDefault(defendingPlayerId, List.of());
        List<UUID> pile1 = List.copyOf(pile1Ids);
        List<UUID> pile2 = groundCreatureIds.stream().filter(id -> !pile1Ids.contains(id)).toList();

        Map<UUID, List<UUID>> pile1ByPlayer = new LinkedHashMap<>(state.pile1IdsByDefendingPlayer());
        Map<UUID, List<UUID>> pile2ByPlayer = new LinkedHashMap<>(state.pile2IdsByDefendingPlayer());
        pile1ByPlayer.put(defendingPlayerId, pile1);
        pile2ByPlayer.put(defendingPlayerId, pile2);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(defendingPlayerId)
                        + " separates nonflying creatures into left and right piles."));

        advanceDefendingPlayerChoice(gameData, new PendingRagingRiver(state.controllerId(), state.attackerIds(),
                state.defendingPlayerByAttackerId(), state.defendingPlayerIds(),
                state.groundCreatureIdsByDefendingPlayer(), pile1ByPlayer, pile2ByPlayer,
                state.nextDefendingPlayerIndex() + 1, state.nextAttackerIndex()));
    }

    /** Completes one attacker's left/right label choice. */
    public void completeAttackerChoice(GameData gameData, boolean choosePile1) {
        PendingRagingRiver state = gameData.pollPendingInteraction(PendingRagingRiver.class);
        if (state == null || state.nextAttackerIndex() >= state.attackerIds().size()) {
            throw new IllegalStateException("No pending Raging River attacker choice");
        }

        UUID attackerId = state.attackerIds().get(state.nextAttackerIndex());
        UUID defendingPlayerId = state.defendingPlayerByAttackerId().get(attackerId);
        List<UUID> chosenPile = (choosePile1
                ? state.pile1IdsByDefendingPlayer()
                : state.pile2IdsByDefendingPlayer())
                .getOrDefault(defendingPlayerId, List.of());
        gameData.ragingRiverBlockRestrictionsThisCombat.compute(attackerId, (ignored, restrictions) -> {
            List<Set<UUID>> updated = restrictions == null ? new ArrayList<>() : new ArrayList<>(restrictions);
            updated.add(Set.copyOf(chosenPile));
            return List.copyOf(updated);
        });

        Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
        String attackerName = attacker == null ? "attacker" : attacker.getCard().getName();
        gameLogService.append(gameData, GameLog.text(attackerName + " is assigned to the "
                + (choosePile1 ? "left" : "right") + " pile for blocking."));

        int nextAttackerIndex = state.nextAttackerIndex() + 1;
        if (nextAttackerIndex < state.attackerIds().size()) {
            PendingRagingRiver nextState = new PendingRagingRiver(state.controllerId(), state.attackerIds(),
                    state.defendingPlayerByAttackerId(), state.defendingPlayerIds(),
                    state.groundCreatureIdsByDefendingPlayer(), state.pile1IdsByDefendingPlayer(),
                    state.pile2IdsByDefendingPlayer(), state.nextDefendingPlayerIndex(), nextAttackerIndex);
            gameData.queueInteraction(nextState);
            beginAttackerChoice(gameData, nextState);
        }
    }

    private void advanceDefendingPlayerChoice(GameData gameData, PendingRagingRiver state) {
        Map<UUID, List<UUID>> pile1ByPlayer = new LinkedHashMap<>(state.pile1IdsByDefendingPlayer());
        Map<UUID, List<UUID>> pile2ByPlayer = new LinkedHashMap<>(state.pile2IdsByDefendingPlayer());
        int nextDefendingPlayerIndex = state.nextDefendingPlayerIndex();
        while (nextDefendingPlayerIndex < state.defendingPlayerIds().size()) {
            UUID defendingPlayerId = state.defendingPlayerIds().get(nextDefendingPlayerIndex);
            List<UUID> groundCreatureIds = state.groundCreatureIdsByDefendingPlayer()
                    .getOrDefault(defendingPlayerId, List.of());
            if (!groundCreatureIds.isEmpty()) {
                PendingRagingRiver nextState = new PendingRagingRiver(state.controllerId(), state.attackerIds(),
                        state.defendingPlayerByAttackerId(), state.defendingPlayerIds(),
                        state.groundCreatureIdsByDefendingPlayer(), pile1ByPlayer, pile2ByPlayer,
                        nextDefendingPlayerIndex, state.nextAttackerIndex());
                gameData.queueInteraction(nextState);
                playerInputService.beginMultiPermanentChoice(gameData, defendingPlayerId, groundCreatureIds,
                        groundCreatureIds.size(),
                        "Separate your nonflying creatures into left and right piles. "
                                + "Select creatures for Left (unselected form Right).");
                return;
            }
            pile1ByPlayer.put(defendingPlayerId, List.of());
            pile2ByPlayer.put(defendingPlayerId, List.of());
            nextDefendingPlayerIndex++;
        }

        PendingRagingRiver nextState = new PendingRagingRiver(state.controllerId(), state.attackerIds(),
                state.defendingPlayerByAttackerId(), state.defendingPlayerIds(),
                state.groundCreatureIdsByDefendingPlayer(), pile1ByPlayer, pile2ByPlayer,
                nextDefendingPlayerIndex, state.nextAttackerIndex());
        gameData.queueInteraction(nextState);
        beginAttackerChoice(gameData, nextState);
    }

    private void beginAttackerChoice(GameData gameData, PendingRagingRiver state) {
        if (state.nextAttackerIndex() >= state.attackerIds().size()) {
            gameData.pollPendingInteraction(PendingRagingRiver.class);
            return;
        }
        Permanent attacker = gameQueryService.findPermanentById(gameData,
                state.attackerIds().get(state.nextAttackerIndex()));
        String attackerName = attacker == null ? "attacker" : attacker.getCard().getName();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(null, state.controllerId(), List.of(),
                "Choose left or right for " + attackerName + ". Yes = Left, No = Right."));
        playerInputService.processNextMayAbility(gameData);
    }

    private UUID findDefendingPlayer(GameData gameData, UUID attackTargetId) {
        if (attackTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackTargetId)
                ? attackTargetId
                : gameQueryService.findPermanentController(gameData, attackTargetId);
    }
}
