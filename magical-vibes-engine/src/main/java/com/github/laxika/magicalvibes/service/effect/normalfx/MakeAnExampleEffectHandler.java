package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMakeAnExample;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAnExampleEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Make an Example's opponent-by-opponent pile separation and sacrifice flow. */
@Component
@RequiredArgsConstructor
public class MakeAnExampleEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeAnExampleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        gameData.recordPileGroupingOrGuess(entry);
        gameData.queueInteraction(new PendingMakeAnExample(
                entry.getControllerId(), opponents, 0, entry.getCard().getName()));
        beginNextOpponent(gameData);
    }

    public void completePileSeparationStep1(GameData gameData, List<UUID> pile1Ids) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        if (state == null) {
            throw new IllegalStateException("No pending Make an Example pile separation");
        }

        List<UUID> pile1 = List.copyOf(pile1Ids);
        List<UUID> pile2 = state.allPermanentIds().stream()
                .filter(id -> !pile1Ids.contains(id))
                .toList();
        gameData.queueInteraction(new PendingPileSeparation(
                state.controllerId(), state.targetPlayerId(), state.allPermanentIds(),
                List.of(), Map.of(), pile1, pile2, CardPileDisposition.MAKE_AN_EXAMPLE, true));

        String separatingPlayer = gameData.playerIdToName.get(state.controllerId());
        String pile1Description = buildPileDescription(gameData, pile1);
        String pile2Description = buildPileDescription(gameData, pile2);
        gameLogService.append(gameData, GameLog.text(separatingPlayer
                + " separates creatures into two piles. Pile 1: " + pile1Description
                + ". Pile 2: " + pile2Description + "."));

        String prompt = "Choose a pile for " + separatingPlayer + " to sacrifice. Yes = Pile 1 ("
                + pile1Description + "), No = Pile 2 (" + pile2Description + ").";
        gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                null, state.targetPlayerId(), List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    public void completePileSeparationStep2(GameData gameData, boolean choosePile1) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        if (state == null) {
            throw new IllegalStateException("No pending Make an Example pile choice");
        }

        UUID sacrificingPlayerId = state.controllerId();
        List<UUID> chosenPile = choosePile1 ? state.pile1Ids() : state.pile2Ids();
        for (UUID permanentId : chosenPile) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !gameQueryService.isCreature(gameData, permanent)
                    || gameQueryService.cantBeSacrificed(gameData, permanent)) {
                continue;
            }
            if (permanentRemovalService.sacrificePermanentToGraveyard(gameData, permanent)) {
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                        gameData, sacrificingPlayerId, permanent.getCard());
                gameData.recordSacrificedPermanent(sacrificingPlayerId, permanent.getCard());
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is sacrificed."));
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }

        PendingMakeAnExample progress = gameData.pollPendingInteraction(PendingMakeAnExample.class);
        if (progress == null) {
            throw new IllegalStateException("Make an Example progress state is missing");
        }

        PendingMakeAnExample next = new PendingMakeAnExample(
                progress.controllerId(), progress.opponentIds(),
                progress.currentOpponentIndex() + 1, progress.sourceName());
        gameData.queueInteraction(next);
        beginNextOpponent(gameData);
    }

    private void beginNextOpponent(GameData gameData) {
        PendingMakeAnExample progress = gameData.peekPendingInteraction(PendingMakeAnExample.class);
        if (progress == null) {
            return;
        }

        int index = progress.currentOpponentIndex();
        while (index < progress.opponentIds().size()) {
            UUID opponentId = progress.opponentIds().get(index);
            List<Permanent> battlefield = gameData.playerBattlefields.get(opponentId);
            List<UUID> creatureIds = battlefield == null
                    ? List.of()
                    : battlefield.stream()
                            .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                            .map(Permanent::getId)
                            .toList();
            if (!creatureIds.isEmpty()) {
                gameData.queueInteraction(new PendingPileSeparation(
                        opponentId, progress.controllerId(), creatureIds, List.of(), Map.of(),
                        List.of(), List.of(), CardPileDisposition.MAKE_AN_EXAMPLE, true));
                playerInputService.beginMultiPermanentChoice(gameData, opponentId, creatureIds,
                        creatureIds.size(), "Separate your creatures into two piles. Select creatures for "
                                + "Pile 1 (unselected creatures form Pile 2).");
                return;
            }
            index++;
            progress = new PendingMakeAnExample(progress.controllerId(), progress.opponentIds(),
                    index, progress.sourceName());
            gameData.pollPendingInteraction(PendingMakeAnExample.class);
            gameData.queueInteraction(progress);
        }

        gameData.pollPendingInteraction(PendingMakeAnExample.class);
    }

    private String buildPileDescription(GameData gameData, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                names.add(permanent.getCard().getName());
            }
        }
        return names.isEmpty() ? "empty" : String.join(", ", names);
    }
}
