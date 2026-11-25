package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMakeAnExample;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAnExampleEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves Make an Example as its three actions (CR 608.2e): each opponent separates their creatures into two piles,
 * in APNAP order (CR 101.4); then the controller chooses one pile of each opponent; then every opponent sacrifices
 * the creatures of their chosen pile, all at once.
 */
@Component
@RequiredArgsConstructor
public class MakeAnExampleEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final DepartureService departureService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeAnExampleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            java.util.Collections.rotate(players, -activeIndex);
        }
        List<UUID> opponents = players.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        gameData.recordPileGroupingOrGuess(entry);
        gameData.queueInteraction(new PendingMakeAnExample(
                entry.getControllerId(), opponents, entry.getCard().getName()));
        beginNextOpponent(gameData);
    }

    /** An opponent has separated their creatures: the piles are recorded and the next opponent separates. */
    public void completePileSeparationStep1(GameData gameData, List<UUID> pile1Ids) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        if (state == null) {
            throw new IllegalStateException("No pending Make an Example pile separation");
        }

        List<UUID> pile1 = List.copyOf(pile1Ids);
        List<UUID> pile2 = state.allPermanentIds().stream()
                .filter(id -> !pile1Ids.contains(id))
                .toList();
        String separatingPlayer = gameData.playerIdToName.get(state.controllerId());
        gameLogService.append(gameData, GameLog.text(separatingPlayer
                + " separates creatures into two piles. Pile 1: " + buildPileDescription(gameData, pile1)
                + ". Pile 2: " + buildPileDescription(gameData, pile2) + "."));

        PendingMakeAnExample progress = pollProgress(gameData);
        gameData.queueInteraction(progress.withSeparation(
                new PendingMakeAnExample.Separation(state.controllerId(), pile1, pile2)));
        beginNextOpponent(gameData);
    }

    /**
     * The controller chose a pile of one opponent. After the last choice every chosen creature is sacrificed as one
     * event; the choice stays pending until they have left, so a completion stopped for a CR 616.1 question is
     * completed again unchanged.
     */
    public void completePileSeparationStep2(GameData gameData, boolean choosePile1) {
        PendingPileSeparation state = gameData.peekPendingInteraction(PendingPileSeparation.class);
        PendingMakeAnExample progress = gameData.peekPendingInteraction(PendingMakeAnExample.class);
        if (state == null || progress == null) {
            throw new IllegalStateException("No pending Make an Example pile choice");
        }

        PendingMakeAnExample chosen = progress.withChosenPile(choosePile1 ? state.pile1Ids() : state.pile2Ids());
        if (chosen.currentChoiceIndex() < chosen.separations().size()) {
            gameData.pollPendingInteraction(PendingPileSeparation.class);
            pollProgress(gameData);
            gameData.queueInteraction(chosen);
            beginNextChoice(gameData);
            return;
        }
        sacrificeChosen(gameData, chosen);
    }

    private void sacrificeChosen(GameData gameData, PendingMakeAnExample chosen) {
        Map<UUID, UUID> sacrificingPlayers = new java.util.HashMap<>();
        for (PendingMakeAnExample.Separation separation : chosen.separations()) {
            separation.pile1Ids().forEach(id -> sacrificingPlayers.put(id, separation.opponentId()));
            separation.pile2Ids().forEach(id -> sacrificingPlayers.put(id, separation.opponentId()));
        }
        List<DepartureIntent> toSacrifice = new ArrayList<>();
        for (UUID permanentId : chosen.chosenToSacrifice()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !gameQueryService.isCreature(gameData, permanent)
                    || !sacrificingPlayers.get(permanentId).equals(
                    gameQueryService.findPermanentController(gameData, permanentId))
                    || gameQueryService.cantBeSacrificed(gameData, permanent)) {
                continue;
            }
            toSacrifice.add(DepartureIntent.sacrifice(permanentId));
        }
        departureService.departNow(gameData, DepartureBatch.of(toSacrifice), new DepartureService.Hooks(
                () -> {
                    gameData.pollPendingInteraction(PendingPileSeparation.class);
                    pollProgress(gameData);
                },
                (intent, permanent, snapshot, removed) -> {
                    if (removed) {
                        UUID sacrificingPlayerId = sacrificingPlayers.get(permanent.getId());
                        ActionEvents.sacrificed(gameData, sacrificingPlayerId, permanent.getCard());
                        gameData.recordSacrificedPermanent(sacrificingPlayerId, permanent.getCard());
                        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is sacrificed."));
                        permanentRemovalService.removeOrphanedAuras(gameData);
                    }
                }));
    }

    /** Asks the next opponent with creatures to separate them; after the last one, the controller chooses. */
    private void beginNextOpponent(GameData gameData) {
        PendingMakeAnExample progress = gameData.peekPendingInteraction(PendingMakeAnExample.class);
        if (progress == null) {
            return;
        }

        while (progress.currentOpponentIndex() < progress.opponentIds().size()) {
            UUID opponentId = progress.opponentIds().get(progress.currentOpponentIndex());
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
            pollProgress(gameData);
            progress = progress.nextOpponent();
            gameData.queueInteraction(progress);
        }

        beginNextChoice(gameData);
    }

    /** Asks the controller to choose the next opponent's pile; after the last one, the piles are sacrificed. */
    private void beginNextChoice(GameData gameData) {
        PendingMakeAnExample progress = gameData.peekPendingInteraction(PendingMakeAnExample.class);
        if (progress == null) {
            return;
        }
        if (progress.currentChoiceIndex() >= progress.separations().size()) {
            pollProgress(gameData);
            return;
        }
        PendingMakeAnExample.Separation separation = progress.separations().get(progress.currentChoiceIndex());
        List<UUID> all = new ArrayList<>(separation.pile1Ids());
        all.addAll(separation.pile2Ids());
        gameData.queueInteraction(new PendingPileSeparation(
                separation.opponentId(), progress.controllerId(), all,
                List.of(), Map.of(), separation.pile1Ids(), separation.pile2Ids(),
                CardPileDisposition.MAKE_AN_EXAMPLE, true));

        String separatingPlayer = gameData.playerIdToName.get(separation.opponentId());
        String prompt = "Choose a pile for " + separatingPlayer + " to sacrifice. Yes = Pile 1 ("
                + buildPileDescription(gameData, separation.pile1Ids()) + "), No = Pile 2 ("
                + buildPileDescription(gameData, separation.pile2Ids()) + ").";
        gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                null, progress.controllerId(), List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    private PendingMakeAnExample pollProgress(GameData gameData) {
        PendingMakeAnExample progress = gameData.pollPendingInteraction(PendingMakeAnExample.class);
        if (progress == null) {
            throw new IllegalStateException("Make an Example progress state is missing");
        }
        return progress;
    }

    private String buildPileDescription(GameData gameData, List<UUID> permanentIds) {
        if (permanentIds.isEmpty()) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                names.add(FaceDownVisibility.publicName(gameData, permanent));
            }
        }
        return names.isEmpty() ? "empty" : String.join(", ", names);
    }
}
