package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FightOrFlightSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    public void completePileSeparationStep1(GameData gameData, List<UUID> pile1Ids) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        List<UUID> pile1 = List.copyOf(pile1Ids);
        List<UUID> pile2 = state.allPermanentIds().stream()
                .filter(id -> !pile1Ids.contains(id))
                .toList();

        gameData.queueInteraction(new PendingPileSeparation(state.controllerId(), state.targetPlayerId(),
                state.allPermanentIds(), state.cards(), state.cardOwners(), pile1, pile2,
                CardPileDisposition.ATTACKERS));

        String pile1Description = buildPileDescription(gameData, pile1);
        String pile2Description = buildPileDescription(gameData, pile2);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(state.controllerId()) + " separates creatures into two piles. Pile 1: "
                        + pile1Description + ". Pile 2: " + pile2Description + "."));

        String prompt = "Choose a pile. Yes = Pile 1 (" + pile1Description + "), No = Pile 2 ("
                + pile2Description + ").";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(null, state.targetPlayerId(), List.of(), prompt));
        playerInputService.processNextMayAbility(gameData);
    }

    public void completePileSeparationStep2(GameData gameData, boolean choosePile1) {
        PendingPileSeparation state = gameData.pollPendingInteraction(PendingPileSeparation.class);
        List<UUID> chosenPile = choosePile1 ? state.pile1Ids() : state.pile2Ids();
        Set<UUID> chosen = new HashSet<>(chosenPile);
        gameData.attackableCreaturesThisTurn.merge(state.targetPlayerId(), Set.copyOf(chosen),
                (existing, next) -> {
                    Set<UUID> intersection = new HashSet<>(existing);
                    intersection.retainAll(next);
                    return Set.copyOf(intersection);
                });

        String playerName = gameData.playerIdToName.get(state.targetPlayerId());
        String pileName = choosePile1 ? "Pile 1" : "Pile 2";
        gameLogService.append(gameData, GameLog.text(playerName + " chooses " + pileName + " for attacking."));
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
        return String.join(", ", names);
    }
}
