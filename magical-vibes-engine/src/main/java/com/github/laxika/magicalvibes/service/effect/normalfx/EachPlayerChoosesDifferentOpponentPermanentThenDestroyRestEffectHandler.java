package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesDifferentOpponentPermanentThenDestroyRestEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves distinct opponent choices before destroying all other matching permanents. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesDifferentOpponentPermanentThenDestroyRestEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesDifferentOpponentPermanentThenDestroyRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (EachPlayerChoosesDifferentOpponentPermanentThenDestroyRestEffect) effect;
        beginNextChoice(gameData, choiceOrder(gameData, entry.getControllerId()), List.of(),
                choiceEffect.filter(), entry.getCard().getId(), entry.getControllerId(), entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesDifferentOpponentPermanentThenDestroyRest context) {
        List<UUID> chosenIds = new ArrayList<>(context.chosenIds());
        chosenIds.addAll(permanentIds);
        beginNextChoice(gameData, context.remainingPlayerIds(), chosenIds, context.filter(),
                context.sourceCardId(), context.sourceControllerId(), context.sourceName());
    }

    private void beginNextChoice(GameData gameData, List<UUID> remainingPlayerIds, List<UUID> chosenIds,
                                 PermanentPredicate filter, UUID sourceCardId, UUID sourceControllerId,
                                 String sourceName) {
        if (remainingPlayerIds.isEmpty()) {
            destructionSupport.performDestroyAllMatchingExcept(gameData, sourceName, chosenIds, filter);
            return;
        }

        UUID choosingPlayerId = remainingPlayerIds.getFirst();
        List<UUID> nextRemainingPlayerIds = remainingPlayerIds.size() > 1
                ? List.copyOf(remainingPlayerIds.subList(1, remainingPlayerIds.size()))
                : List.of();
        List<UUID> candidates = opponentPermanentIds(gameData, choosingPlayerId, chosenIds, filter,
                sourceCardId, sourceControllerId);
        if (candidates.isEmpty()) {
            beginNextChoice(gameData, nextRemainingPlayerIds, chosenIds, filter, sourceCardId,
                    sourceControllerId, sourceName);
            return;
        }
        if (candidates.size() == 1) {
            List<UUID> nextChosenIds = new ArrayList<>(chosenIds);
            nextChosenIds.add(candidates.getFirst());
            beginNextChoice(gameData, nextRemainingPlayerIds, nextChosenIds, filter, sourceCardId,
                    sourceControllerId, sourceName);
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData, choosingPlayerId, candidates, 1,
                new MultiPermanentChoiceContext.EachPlayerChoosesDifferentOpponentPermanentThenDestroyRest(
                        nextRemainingPlayerIds, chosenIds, filter, sourceCardId, sourceControllerId, sourceName),
                sourceName + " — choose a nonland permanent you don't control to keep.");
    }

    private List<UUID> opponentPermanentIds(GameData gameData, UUID choosingPlayerId, List<UUID> chosenIds,
                                             PermanentPredicate filter, UUID sourceCardId,
                                             UUID sourceControllerId) {
        Set<UUID> alreadyChosen = new HashSet<>(chosenIds);
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCardId)
                .withSourceControllerId(sourceControllerId);
        List<UUID> candidateIds = new ArrayList<>();
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(choosingPlayerId)) {
                continue;
            }
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
                if (!alreadyChosen.contains(permanent.getId())
                        && predicateEvaluationService.matchesPermanentPredicate(
                        permanent, filter, filterContext)) {
                    candidateIds.add(permanent.getId());
                }
            }
        }
        return candidateIds;
    }

    private List<UUID> choiceOrder(GameData gameData, UUID controllerId) {
        List<UUID> order = new ArrayList<>();
        order.add(controllerId);
        for (UUID playerId : apnapPlayers(gameData)) {
            if (!playerId.equals(controllerId)) {
                order.add(playerId);
            }
        }
        return order;
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return orderedPlayers;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
        rotated.addAll(orderedPlayers.subList(0, activeIndex));
        return rotated;
    }
}
