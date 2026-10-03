package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPermanentsThenPhaseOutRestEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Disciple of Caelus Nin's ordered permanent choices and phase-out sweep. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesPermanentsThenPhaseOutRestEffectHandler
        implements NormalEffectHandlerBean {

    private final PhasingService phasingService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesPermanentsThenPhaseOutRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (EachPlayerChoosesPermanentsThenPhaseOutRestEffect) effect;
        step(gameData, playersStartingWith(gameData, entry.getControllerId()), 0, List.of(),
                choiceEffect.maxCount(), entry.getSourcePermanentId(), entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, List<UUID> chosenIds,
            MultiPermanentChoiceContext.EachPlayerChoosesPermanentsThenPhaseOutRestChoice context) {
        List<UUID> allChosenIds = new ArrayList<>(context.chosenIds());
        allChosenIds.addAll(chosenIds);
        step(gameData, context.playerIds(), context.playerIndex() + 1, allChosenIds,
                context.maxCount(), context.sourcePermanentId(), context.sourceName());
    }

    private void step(GameData gameData, List<UUID> playerIds, int playerIndex,
            List<UUID> chosenIds, int maxCount, UUID sourcePermanentId, String sourceName) {
        List<UUID> allChosenIds = new ArrayList<>(chosenIds);

        for (int currentPlayerIndex = playerIndex; currentPlayerIndex < playerIds.size(); currentPlayerIndex++) {
            UUID playerId = playerIds.get(currentPlayerIndex);
            List<UUID> candidates = controlledPermanentIds(gameData, playerId);

            if (candidates.size() <= maxCount) {
                allChosenIds.addAll(candidates);
                continue;
            }

            playerInputService.beginMultiPermanentChoice(
                    gameData, playerId, candidates, maxCount,
                    new MultiPermanentChoiceContext.EachPlayerChoosesPermanentsThenPhaseOutRestChoice(
                            playerIds, currentPlayerIndex, allChosenIds, maxCount,
                            sourcePermanentId, sourceName),
                    sourceName + " — choose up to " + maxCount + " permanents to keep.");
            return;
        }

        finish(gameData, allChosenIds, sourcePermanentId);
    }

    private void finish(GameData gameData, List<UUID> chosenIds, UUID sourcePermanentId) {
        Set<UUID> chosen = new HashSet<>(chosenIds);
        List<Permanent> toPhaseOut = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.stream()
                .filter(permanent -> sourcePermanentId == null
                        || !sourcePermanentId.equals(permanent.getId()))
                .filter(permanent -> !chosen.contains(permanent.getId()))
                .forEach(toPhaseOut::add));
        phasingService.phaseOut(gameData, toPhaseOut);
    }

    private List<UUID> controlledPermanentIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .map(Permanent::getId)
                .toList();
    }

    private List<UUID> playersStartingWith(GameData gameData, UUID firstPlayerId) {
        List<UUID> players = new ArrayList<>(gameData.orderedPlayerIds);
        int firstIndex = players.indexOf(firstPlayerId);
        if (firstIndex <= 0) {
            return players;
        }
        List<UUID> rotated = new ArrayList<>(players.subList(firstIndex, players.size()));
        rotated.addAll(players.subList(0, firstIndex));
        return rotated;
    }
}
