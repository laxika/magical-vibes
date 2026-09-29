package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesOwnPermanentsToExileUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves sequential exact-count choices by each player for a source-linked exile. */
@Component
@RequiredArgsConstructor
public class EachPlayerChoosesOwnPermanentsToExileUntilSourceLeavesEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesOwnPermanentsToExileUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (EachPlayerChoosesOwnPermanentsToExileUntilSourceLeavesEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null
                || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            return;
        }

        beginNextChoice(gameData, orderStartingWith(gameData, gameData.activePlayerId), List.of(),
                exileEffect.filter(), exileEffect.maxCount(), entry.getCard(), entry.getControllerId(),
                sourcePermanentId, entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.EachPlayerChoosesOwnPermanentsToExileUntilSourceLeaves context) {
        List<UUID> chosenIds = new ArrayList<>(context.chosenIds());
        chosenIds.addAll(permanentIds);
        beginNextChoice(gameData, context.remainingPlayerIds(), chosenIds, context.filter(),
                context.maxCount(), context.sourceCard(), context.sourceControllerId(),
                context.sourcePermanentId(), context.sourceName());
    }

    private void beginNextChoice(GameData gameData, List<UUID> remainingPlayerIds, List<UUID> chosenIds,
                                 PermanentPredicate filter, int maxCount, Card sourceCard,
                                 UUID sourceControllerId, UUID sourcePermanentId, String sourceName) {
        if (remainingPlayerIds.isEmpty()) {
            exileChosen(gameData, chosenIds, sourcePermanentId, sourceCard);
            return;
        }

        UUID choosingPlayerId = remainingPlayerIds.getFirst();
        List<UUID> nextRemainingPlayerIds = remainingPlayerIds.size() > 1
                ? List.copyOf(remainingPlayerIds.subList(1, remainingPlayerIds.size()))
                : List.of();
        List<UUID> candidates = ownPermanentIds(gameData, choosingPlayerId, filter,
                sourceCard.getId(), sourceControllerId);
        if (candidates.isEmpty()) {
            beginNextChoice(gameData, nextRemainingPlayerIds, chosenIds, filter, maxCount,
                    sourceCard, sourceControllerId, sourcePermanentId, sourceName);
            return;
        }

        int requiredCount = Math.min(maxCount, candidates.size());
        playerInputService.beginMultiPermanentChoice(gameData, choosingPlayerId, candidates, requiredCount,
                new MultiPermanentChoiceContext.EachPlayerChoosesOwnPermanentsToExileUntilSourceLeaves(
                        nextRemainingPlayerIds, chosenIds, filter, sourceCard, sourceControllerId,
                        sourcePermanentId, sourceName, maxCount, requiredCount),
                sourceName + " — choose " + requiredCount + " creature" + (requiredCount == 1 ? "" : "s")
                        + " you control to exile.");
    }

    private List<UUID> ownPermanentIds(GameData gameData, UUID playerId, PermanentPredicate filter,
                                       UUID sourceCardId, UUID sourceControllerId) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(sourceCardId)
                .withSourceControllerId(sourceControllerId);
        List<UUID> candidateIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext)) {
                candidateIds.add(permanent.getId());
            }
        }
        return candidateIds;
    }

    private void exileChosen(GameData gameData, List<UUID> chosenIds, UUID sourcePermanentId,
                             Card sourceCard) {
        for (UUID permanentId : chosenIds.stream().distinct().toList()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                Card exiledCard = permanent.getOriginalCard();
                exileSupport.exilePermanentAndTrackWithSource(
                        gameData, permanent, sourcePermanentId, sourceCard);
                ExiledCardEntry exiledEntry = gameData.findExiledCard(exiledCard.getId());
                if (exiledEntry != null && !exiledCard.isToken()) {
                    gameData.addExileReturnOnPermanentLeave(sourcePermanentId,
                            new PendingExileReturn(exiledCard, exiledEntry.ownerId()));
                }
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
