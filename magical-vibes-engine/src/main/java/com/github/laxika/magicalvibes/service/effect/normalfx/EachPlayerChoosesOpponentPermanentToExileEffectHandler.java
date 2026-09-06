package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesOpponentPermanentToExileEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves sequential up-to-one choices for permanents controlled by each player's opponents. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesOpponentPermanentToExileEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesOpponentPermanentToExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (EachPlayerChoosesOpponentPermanentToExileEffect) effect;
        UUID controllerId = entry.getControllerId();
        beginNextChoice(gameData, orderStartingWith(gameData, controllerId), List.of(),
                exileEffect.filter(), entry.getCard().getId(), controllerId, entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesOpponentPermanentToExile context) {
        List<UUID> chosenIds = new ArrayList<>(context.chosenIds());
        chosenIds.addAll(permanentIds);
        beginNextChoice(gameData, context.remainingPlayerIds(), chosenIds, context.filter(),
                context.sourceCardId(), context.sourceControllerId(), context.sourceName());
    }

    private void beginNextChoice(GameData gameData, List<UUID> remainingPlayerIds, List<UUID> chosenIds,
                                 PermanentPredicate filter, UUID sourceCardId, UUID sourceControllerId,
                                 String sourceName) {
        if (remainingPlayerIds.isEmpty()) {
            exileChosen(gameData, chosenIds, sourceName);
            return;
        }

        UUID choosingPlayerId = remainingPlayerIds.getFirst();
        List<UUID> nextRemainingPlayerIds = remainingPlayerIds.size() > 1
                ? List.copyOf(remainingPlayerIds.subList(1, remainingPlayerIds.size()))
                : List.of();
        List<UUID> candidates = opponentPermanentIds(gameData, choosingPlayerId, filter,
                sourceCardId, sourceControllerId);
        if (candidates.isEmpty()) {
            beginNextChoice(gameData, nextRemainingPlayerIds, chosenIds, filter, sourceCardId,
                    sourceControllerId, sourceName);
            return;
        }

        playerInputService.beginMultiPermanentChoice(gameData, choosingPlayerId, candidates, 1,
                new MultiPermanentChoiceContext.EachPlayerChoosesOpponentPermanentToExile(
                        nextRemainingPlayerIds, chosenIds, filter, sourceCardId, sourceControllerId, sourceName),
                sourceName + " — Choose up to one permanent an opponent controls to exile.");
    }

    private List<UUID> opponentPermanentIds(GameData gameData, UUID choosingPlayerId,
                                             PermanentPredicate filter, UUID sourceCardId,
                                             UUID sourceControllerId) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCardId)
                .withSourceControllerId(sourceControllerId);
        List<UUID> candidateIds = new ArrayList<>();
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(choosingPlayerId)) {
                continue;
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext)) {
                    candidateIds.add(permanent.getId());
                }
            }
        }
        return candidateIds;
    }

    private void exileChosen(GameData gameData, List<UUID> chosenIds, String sourceName) {
        for (UUID permanentId : new LinkedHashSet<>(chosenIds)) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                exileSupport.exilePermanentAndLog(gameData, permanent, sourceName);
            }
        }
    }

    private List<UUID> orderStartingWith(GameData gameData, UUID firstPlayerId) {
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int firstIndex = orderedPlayerIds.indexOf(firstPlayerId);
        if (firstIndex <= 0) {
            return orderedPlayerIds;
        }
        List<UUID> rotated = new ArrayList<>(orderedPlayerIds.subList(firstIndex, orderedPlayerIds.size()));
        rotated.addAll(orderedPlayerIds.subList(0, firstIndex));
        return rotated;
    }
}
